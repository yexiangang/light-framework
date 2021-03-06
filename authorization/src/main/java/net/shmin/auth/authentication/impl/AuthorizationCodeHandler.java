package net.shmin.auth.authentication.impl;

import net.shmin.auth.client.IClientManager;
import net.shmin.auth.client.impl.ClientManager;
import net.shmin.auth.token.Token;
import net.shmin.auth.token.TokenType;
import net.shmin.auth.util.WebUtil;
import net.shmin.core.dto.CommonResponseDTO;
import net.shmin.core.util.PropertiesUtil;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by benjamin on 9/12/14.
 * 授权码模式实现。
 */
@Component("authorizationCodeHandler")
public class AuthorizationCodeHandler extends GrantTypeAuthorizationHandlerAdapter {

    private IClientManager<String> clientManager;

    public AuthorizationCodeHandler() {
        super();
        clientManager = new ClientManager();
    }

    @Override
    public void handleAuthCodeGrantType(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //尝试从cookie中读取user
        String username = WebUtil.getCookieValue(request, username_cookie_name);
        //如果cookie中没有user 第一,没有登录,第二,正在登录
        if (username == null) {
            username = request.getParameter(requestParamUsername);
        }
        String responseType = request.getParameter(RESPONSE_TYPE);
        String clientId = request.getParameter(CLIENT_ID);
        String redirect_uri = request.getParameter(REDIRECT_URI);
        if (responseType != null
                && !responseType.isEmpty()
                && responseType.equals("code")
                && request.getPathInfo().contains("authorize")) {
            //未登录过 必须登录
            if (username == null) {
                try {
                    request.getRequestDispatcher("/loginPage.jsp").forward(request, response);
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ServletException e) {
                    e.printStackTrace();
                }
            }
            // 如果cookie是空 而且用户名非空,那就是正在登录验证
            if (WebUtil.getCookieValue(request, DEFAULT_USER_COOKIE_NAME) == null && username != null) {
                CommonResponseDTO result = login(request);
                if (!result.isSuccess()) {
                    try {
                        WebUtil.replyNoAccess(request, response, result.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }

            String state = request.getParameter(STATE);
            if (clientManager.checkClientId(clientId)) {
                Token token = getAuthTokenGenerator().generateAccessToken(true);
                getTokenProvider().saveToken(username, token);
//        req.getRequestDispatcher("access_token?code="+token.getValue()+"&redirect_uri="+redirect_uri+"&state="+state+"&grant_type=authorization_code").forward(req, resp);
                if (redirect_uri == null || redirect_uri.isEmpty()) {
                    redirect_uri = PropertiesUtil.getString(REDIRECT_URI);
                }
                StringBuilder stringBuilder = new StringBuilder(redirect_uri);
                stringBuilder.append("?code=");
                stringBuilder.append(token.getValue());
                stringBuilder.append("&client_id=");
                stringBuilder.append(clientId);
                //access_token的回调uri
                String redirect_uri_access_token = PropertiesUtil.getString(REDIRECT_URI, redirect_uri);
                stringBuilder.append("&redirect_uri=");
                stringBuilder.append(redirect_uri_access_token);
                if (state != null && !state.isEmpty()) {
                    stringBuilder.append("&state=");
                    stringBuilder.append(state);
                }
//        if(WebUtil.getCookieValue(request, DEFAULT_USER_COOKIE_NAME) == null){
//          Cookie cookie = new Cookie(, username);
//          cookie.setPath(request.getContextPath());
//          response.addCookie(cookie);
//          stringBuilder.append("&username=");
//          stringBuilder.append(username);
//        }
                try {
                    response.sendRedirect(stringBuilder.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //直接获取token
        } else if (request.getPathInfo().contains("access_token")) {
            if (clientManager.checkClientId(clientId)) {
                String code = request.getParameter("code");
                if (code == null || code.isEmpty()) {
                    try {
                        WebUtil.replyNoAccess(request, response, CommonResponseDTO.error(10009, "No Request code found").toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Token token_code = new Token();
                    token_code.setTokenType(TokenType.authorizationCode);
                    if (username == null) {
                        username = (String) request.getAttribute("user");
                    }
                    boolean result = getTokenProvider().checkToken(username, token_code);
                    if (result) {
                        Token token = getAuthTokenGenerator().generateAccessToken(false);
                        getTokenProvider().saveToken(username, token);
                        try {
                            response.sendRedirect(redirect_uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        super.handleAuthCodeGrantType(request, response);
    }
}
