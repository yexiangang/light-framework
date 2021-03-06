package net.shmin.auth.token.impl;

import net.shmin.auth.token.IAuthTokenGenerator;
import net.shmin.auth.token.IAuthTokenProvider;
import net.shmin.auth.token.Token;
import net.shmin.auth.token.TokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by benjamin on 2017/3/29.
 */
@Component
public abstract class AbstractTokenProvider implements IAuthTokenProvider {

    private static Logger logger = LoggerFactory.getLogger(AbstractTokenProvider.class);

    @Autowired
    protected IAuthTokenGenerator tokenGenerator;

    @Override
    public Token newTokenFromRefreshToken(String key, String refreshTokenValue) {

        Token refreshToken = new Token();
        refreshToken.setTokenType(TokenType.refreshToken);
        refreshToken.setValue(refreshTokenValue);

        if(checkToken(key, refreshToken)){
            deleteToken(key, refreshToken.getTokenType());
            Token token = tokenGenerator.generateAccessToken(true);
            saveToken(key, token);
            saveToken(key, token);
            logger.info("通过RefreshToken {} 重新生成了新的token {}", refreshToken.toString(), token.toString());
            return token;
        }
        return null;
    }
}
