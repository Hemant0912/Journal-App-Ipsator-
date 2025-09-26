package net.hemjournalApp.filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${rate.limit.requests:10}")
    private int maxRequests;

    @Value("${rate.limit.windowSeconds:60}")
    private int windowSeconds;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String identifier;
        if (auth != null && auth.isAuthenticated() && auth.getName() != null && !"anonymousUser".equals(auth.getName())) {
            identifier = "user:" + auth.getName();
        } else {
            identifier = "ip:" + request.getRemoteAddr();
        }

        String key = "rl:" + identifier + ":" + request.getServletPath();
        Long current = redisTemplate.opsForValue().increment(key);

        if (current != null && current == 1L) {
            // first increment -> set expiry for the window
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }

        if (current != null && current > maxRequests) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests - try again later");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
