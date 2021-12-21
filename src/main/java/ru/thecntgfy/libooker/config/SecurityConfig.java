package ru.thecntgfy.libooker.config;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.thecntgfy.libooker.security.JwtAuthenticationEntryPoint;
import ru.thecntgfy.libooker.security.JwtAuthenticationFilter;
import ru.thecntgfy.libooker.security.JwtProvider;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
//TODO: Move Spring Boot Admin to docker container
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserDetailsService userDetailsService;
    private final AdminServerProperties adminServer;

    private final JwtAuthenticationEntryPoint entryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityProperties securityProperties;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Bean
    public PasswordEncoder passwordEncoder() {
        return encoder;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        String username = securityProperties.getUser().getName();
        String password = encoder.encode(securityProperties.getUser().getPassword());
        auth.inMemoryAuthentication().withUser(username)
                .password(password).roles("USER");

        auth.userDetailsService(userDetailsService).passwordEncoder(encoder);
    }

    //TODO: Check
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler =
                new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(adminServer.path("/"));

        http
                .cors(Customizer.withDefaults())
                .csrf().disable()
                // Requests
                .authorizeRequests()
                .antMatchers(adminServer.path("/assets/**")).permitAll()
                .antMatchers(adminServer.path("/actuator/info")).permitAll()
                .antMatchers(adminServer.path("/actuator/health")).permitAll()
                .antMatchers(adminServer.path("/login")).permitAll()
                .antMatchers("/auth/**").permitAll()
                .anyRequest().authenticated()
                .and()
                // Login
                .formLogin().loginPage(adminServer.path("/login")).successHandler(successHandler)
                .and()
                //Log out
                .logout().logoutUrl(adminServer.path("/logout"))
                .and()
                .httpBasic(Customizer.withDefaults())
                // Exceptions
                .exceptionHandling()
                .authenticationEntryPoint(entryPoint)
                .and()
                // Disable sessions
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // Custom filters
                .antMatcher("/bookings/**").addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );
    }
}
