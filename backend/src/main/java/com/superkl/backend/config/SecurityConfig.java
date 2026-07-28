package com.superkl.backend.config;

import com.superkl.backend.utils.JwtAuthenticationFilter;
import com.superkl.backend.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtUtil jwtUtil;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil);
    }

    @Bean// bean注解，告诉 Spring 这是一个 Bean，需要被扫描到
    // 这里就是我们最关键的配置，告诉Spring Security对这些请求进行处理
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 开启 CORS 配置，允许跨域请求
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 禁用 CSRF 防护，CSRF：跨站请求伪造，这里你可以选择打开，因为我们在Cookie中提到了SameSite=Lax，防止CSRF攻击。
                // 但是，如果你的前端是 H5 应用，或者你没有使用 SameSite=Lax，那么你需要打开 CSRF 防护。千万不要开发的时候关上，生产的时候忘记开了！！！
                .csrf(csrf -> csrf.disable())
                // 禁用表单登录，因为我们使用 JWT 认证，不需要表单登录。
                // 如果启动，那么Spring boot会提供一个基本的表单登录页面，但是我们不需要，所以我们禁用它。
                .formLogin(formLogin -> formLogin.disable())
                // 禁用 HTTP 基本认证，因为我们使用 JWT 认证，不需要 HTTP 基本认证。
                // 如果启动，那么Spring boot会提供一个基本的 HTTP 基本认证页面，但是我们不需要，所以我们禁用它。
                .httpBasic(httpBasic -> httpBasic.disable())
                // 重点来了！看过来：
                // 这里是配置 Spring Security 对请求的授权规则。
                // 我们需要指定哪些请求需要登录，哪些请求不需要登录。
                // 这里我们简单地指定所有请求都需要登录，但是`/auth/login`路径不需要登录。
                // permitAll() 表示该路径的全部请求
                // authenticated() 表示该路径的全部请求需要登录
                // anyRequest() 表示其他所有请求，特别是你没有明确指出的路径，都需要登录。
                // hasRole("role") 表示该路径的全部请求需要登录，且用户角色必须是 role 角色。
                .authorizeHttpRequests(auth -> auth
                        // 登录全面放行
                        .requestMatchers("/api/admin/login", "/api/warehouse/login").permitAll()
                        // 前端收银员可以访问所有接口
                        .requestMatchers("/api/employee/verify/**", "/api/order/draft/**",
                                "/api/order/complete","/api/order/delete/**","/api/order/search/wareHouse/**").hasRole("WAREHOUSE")
                        .requestMatchers( "/api/stock/**",
                                "/api/product/search/**", "/api/productSku/search/**",
                                "/api/productSku/scan/**", "/api/warehouse/logout","/api/order/search/**")
                        .hasAnyRole("ADMIN", "WAREHOUSE")
                        // 管理员可以访问所有接口
                        .requestMatchers("/api/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                // 如果启动，那么Spring boot会提供一个会话管理页面，但是我们不需要，所以我们禁用它。
                // 但是，如果你的前端是 H5 应用，或者你没有使用 SameSite=Lax，那么你需要打开会话管理。千万不要开发的时候关上，生产的时候忘记开了！！！
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 这里也是重点！ 一定要添加，否则Spring Security还是不认识你传入的Token信息
                // 这里使用的是JwtAuthenticationFilter这个类，它负责解析Token并验证用户信息
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean // 注册 CORS 配置源，用于跨域请求 —— 允许所有来源的请求
    public CorsConfigurationSource corsConfigurationSource() {
        // 配置 CORS 允许所有来源的请求
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许所有来源的请求
        configuration.setAllowedOrigins(List.of("https://kl-koole.com","http://localhost:5173"));  // 前端地址
        // 允许所有请求方法
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许 Content-Type 请求头
        // 注意这里不建议使用通配符，因为这样会导致前端在发送请求时，会因为 Content-Type 头为空而被拒绝。
        configuration.setAllowedHeaders(List.of("Content-Type"));
        // 允许携带 Cookie，重点，否则前端无法发送 Cookie 到后端
        // 前端需要设置 withCredentials: true 才能发送 Cookie 到后端
        configuration.setAllowCredentials(true);  // 关键：允许携带 Cookie
        // 允许跨域请求的缓存时间，单位秒
        configuration.setMaxAge(3600L);

        // 注册 CORS 配置源，用于跨域请求 —— 允许所有来源的请求
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 注册 CORS 配置，对所有路径都生效
        // 允许所有来源的请求
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
