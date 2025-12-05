package com.ghostcheck.service.osint;

import com.ghostcheck.config.HibpProperties;
import com.ghostcheck.entity.BreachRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class HIBPClient {
    private final RestTemplate restTemplate;
    private final HibpProperties props;

    public HIBPClient(RestTemplate restTemplate, HibpProperties props) {
        this.restTemplate = restTemplate;
        this.props = props;
    }



    @SuppressWarnings("unchecked")
    public List<BreachRecord> breachesForEmail(String email) {
        if (props.getApiKey() == null || props.getApiKey().isBlank()) {
            // Load from bundled static dataset
            try {
                var resource = new ClassPathResource("data/breaches.json");
                String json = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                // very small JSON parsing without external libs
                List<Map<String,Object>> items = parseArrayOfObjects(json);
                List<BreachRecord> mapped = new ArrayList<>();
                for (Map<String,Object> m : items) {
                    String targetEmail = String.valueOf(m.getOrDefault("email", "")).toLowerCase();
                    if (!targetEmail.equals(email.toLowerCase())) continue;
                    String name = String.valueOf(m.getOrDefault("Name", "Unknown"));
                    String desc = String.valueOf(m.getOrDefault("Description", ""));
                    String breachDateStr = String.valueOf(m.getOrDefault("BreachDate", null));
                    Instant breachDate = Instant.now();
                    if (breachDateStr != null && !breachDateStr.equals("null")) {
                        try {
                            java.time.LocalDate d = java.time.LocalDate.parse(breachDateStr);
                            breachDate = d.atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
                        } catch (Exception ignored) {}
                    }
                    String exposedJson = toJson(m);
                    BreachRecord br = BreachRecord.builder()
                            .sourceName(name)
                            .breachDate(breachDate)
                            .description(desc)
                            .exposedData(exposedJson)
                            .addedDate(Instant.now())
                            .pwnCount(parsePwnCount(m))
                            .build();
                    mapped.add(br);
                }
                return mapped;
            } catch (Exception e) {
                return List.of();
            }
        }
        try {
            String url = props.getBaseUrl() + "/breachedaccount/" + email + "?truncateResponse=false";
            HttpHeaders headers = new HttpHeaders();
            headers.add("hibp-api-key", props.getApiKey());
            headers.add(HttpHeaders.USER_AGENT, "GhostCheck/1.0");
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            List<Object> body = restTemplate.exchange(url, HttpMethod.GET, entity, List.class).getBody();
            List<BreachRecord> mapped = new ArrayList<>();
            if (body != null) {
                for (Object item : body) {
                    Map<String, Object> m = (Map<String, Object>) item;
                    String name = String.valueOf(m.getOrDefault("Name", "Unknown"));
                    String desc = String.valueOf(m.getOrDefault("Description", ""));
                    String breachDateStr = String.valueOf(m.getOrDefault("BreachDate", null));
                    Instant breachDate = Instant.now();
                    if (breachDateStr != null && !breachDateStr.equals("null")) {
                        try {
                            java.time.LocalDate d = java.time.LocalDate.parse(breachDateStr);
                            breachDate = d.atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
                        } catch (Exception ignored) {}
                    }
                    String exposedJson = toJson(m);
                    BreachRecord br = BreachRecord.builder()
                            .sourceName(name)
                            .breachDate(breachDate)
                            .description(desc)
                            .exposedData(exposedJson)
                            .addedDate(Instant.now())
                            .pwnCount(parsePwnCount(m))
                            .build();
                    mapped.add(br);
                }
            }
            // simple rate limit sleep
            try { Thread.sleep(props.getRateLimitMs()); } catch (InterruptedException ignored) {}
            return mapped;
        } catch (Exception e) {
            return List.of();
        }
    }

    // naive parser for an array of flat objects (keys and simple values only)
    private List<Map<String,Object>> parseArrayOfObjects(String json) {
        // This is a minimal parser sufficient for the bundled dataset structure
        List<Map<String,Object>> out = new ArrayList<>();
        String trimmed = json.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return out;
        String body = trimmed.substring(1, trimmed.length()-1);
        String[] objs = body.split("},\\s*\\{");
        for (String raw : objs) {
            String o = raw.trim();
            if (!o.startsWith("{")) o = "{" + o;
            if (!o.endsWith("}")) o = o + "}";
            out.add(parseObject(o));
        }
        return out;
    }

    private Map<String,Object> parseObject(String json) {
        Map<String,Object> map = new java.util.LinkedHashMap<>();
        String inner = json.trim();
        if (inner.startsWith("{")) inner = inner.substring(1);
        if (inner.endsWith("}")) inner = inner.substring(0, inner.length()-1);
        // split on commas that are not inside brackets
        int depth = 0; StringBuilder token = new StringBuilder(); List<String> parts = new ArrayList<>();
        for (int i=0;i<inner.length();i++) {
            char c = inner.charAt(i);
            if (c=='[') depth++; else if (c==']') depth--;
            if (c==',' && depth==0) { parts.add(token.toString()); token.setLength(0); }
            else token.append(c);
        }
        if (!token.isEmpty()) parts.add(token.toString());
        for (String part : parts) {
            String[] kv = part.split(":",2);
            if (kv.length<2) continue;
            String k = kv[0].trim();
            if (k.startsWith("\"")) k = k.substring(1);
            if (k.endsWith("\"")) k = k.substring(0,k.length()-1);
            String v = kv[1].trim();
            Object val;
            if (v.startsWith("\"")) {
                String s = v;
                if (s.endsWith(",")) s = s.substring(0,s.length()-1);
                if (s.endsWith("\"")) s = s.substring(1, s.length()-1);
                else s = s.substring(1);
                val = s;
            } else if (v.startsWith("[")) {
                // split simple array of strings
                String arr = v;
                if (arr.endsWith(",")) arr = arr.substring(0,arr.length()-1);
                if (arr.endsWith("]")) arr = arr.substring(1, arr.length()-1);
                else arr = arr.substring(1);
                List<String> list = new ArrayList<>();
                for (String item : arr.split(",")) {
                    String s = item.trim();
                    if (s.startsWith("\"")) s = s.substring(1);
                    if (s.endsWith("\"")) s = s.substring(0,s.length()-1);
                    list.add(s);
                }
                val = list;
            } else if (v.equals("null")) {
                val = null;
            } else {
                // number or bool
                if (v.endsWith(",")) v = v.substring(0,v.length()-1);
                if (v.equals("true") || v.equals("false")) val = Boolean.valueOf(v);
                else {
                    try { val = Integer.valueOf(v); } catch (Exception ex) { val = v; }
                }
            }
            map.put(k, val);
        }
        return map;
    }

    private String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof java.util.Map<?,?> map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (var e : map.entrySet()) {
                if (!first) sb.append(',');
                first = false;
                sb.append('"').append(String.valueOf(e.getKey()).replace("\"","\\\"")).append('"').append(':');
                sb.append(toJson(e.getValue()));
            }
            sb.append('}');
            return sb.toString();
        }
        if (obj instanceof java.util.Collection<?> col) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (var e : col) {
                if (!first) sb.append(',');
                first = false;
                sb.append(toJson(e));
            }
            sb.append(']');
            return sb.toString();
        }
        if (obj instanceof Number || obj instanceof Boolean) return String.valueOf(obj);
        return '"' + String.valueOf(obj).replace("\"","\\\"") + '"';
    }

    private Long parsePwnCount(Map<String,Object> m) {
        Object pc = m.getOrDefault("PwnCount", null);
        if (pc == null) return null;
        try {
            if (pc instanceof Number n) return n.longValue();
            return Long.parseLong(String.valueOf(pc));
        } catch (Exception e) {
            return null;
        }
    }
}
