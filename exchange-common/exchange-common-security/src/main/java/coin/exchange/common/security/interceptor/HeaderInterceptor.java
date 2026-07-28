package coin.exchange.common.security.interceptor;

import coin.exchange.api.user.model.LoginVo;
import coin.exchange.common.core.constant.SecurityConstants;
import coin.exchange.common.core.context.SecurityContextHolder;
import coin.exchange.common.core.utils.ServletUtils;
import coin.exchange.common.security.utils.SecurityUtils;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

/**
 * 自定义请求头拦截器，将网关透传的用户信息封装到线程变量中。
 */
@Slf4j
public class HeaderInterceptor implements AsyncHandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        if (!(handler instanceof HandlerMethod))
        {
            return true;
        }

        SecurityContextHolder.setUserId(ServletUtils.getHeader(request, SecurityConstants.DETAILS_USER_ID));
        SecurityContextHolder.setUserName(ServletUtils.getHeader(request, SecurityConstants.DETAILS_USERNAME));
        SecurityContextHolder.setUserKey(ServletUtils.getHeader(request, SecurityConstants.USER_KEY));

        String token = SecurityUtils.getToken();
        if (StringUtils.isNotEmpty(token))
        {
            LoginVo loginVo = new LoginVo();
            loginVo.setId(SecurityContextHolder.get(SecurityConstants.DETAILS_USER_ID));
            loginVo.setUsername(SecurityContextHolder.getUserName());
            loginVo.setToken(token);
            SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginVo);
            log.debug("设置当前登录用户上下文: {}", loginVo);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception
    {
        SecurityContextHolder.remove();
    }
}
