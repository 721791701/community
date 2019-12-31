package com.bbgu.zmz.community.controller;

import com.bbgu.zmz.community.dto.AccessTokenDTO;
import com.bbgu.zmz.community.dto.GithubUser;
import com.bbgu.zmz.community.model.User;
import com.bbgu.zmz.community.provider.GithubProvider;
import com.bbgu.zmz.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Controller
public class AuthorizeController {
    @Autowired
    private GithubProvider githubProvider;

    @Value("${github.client.id}")
    private String clientId;

    @Value("${github.client.secret}")
    private String clientSecret;

    @Value("${github.redirect.uri}")
    private String redirectUri;

    @Autowired
    private UserService userService;
    @GetMapping("/callback")
    public String callback(@RequestParam(name="code") String code,
                           @RequestParam(name="state") String url,
                           HttpServletResponse response){
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO();
        accessTokenDTO.setCode(code);
        accessTokenDTO.setClient_id(clientId);
        accessTokenDTO.setClient_secret(clientSecret);
        accessTokenDTO.setRedirect_uri(redirectUri);
        accessTokenDTO.setState(url);
        String accessToken = githubProvider.getAccessTokenDTO(accessTokenDTO);
        GithubUser githubUser = githubProvider.getUser(accessToken);
        if(githubUser !=null && githubUser.getId() != null){
            User user = new User();
            String token = UUID.randomUUID().toString();
            user.setToken(token);
            user.setName(githubUser.getName());
            user.setAccountId(githubUser.getId());
            user.setAvatarUrl(githubUser.getAvatar_url());
            user.setStatus(1);
            if(githubUser.getId() == 56061468){
                user.setRole("管理员");
            }else{
                user.setRole("社区用户");
            }
            userService.createOrUpdate(user);
            response.addCookie(new Cookie("token",token));
            //request.getSession().setAttribute("user",githubUser);
            int index = url.lastIndexOf("/");
            String str = url.substring(index+1);
            if(str.equals("reg") || str.equals("login")){
                return "redirect:/";
            }else{
                return "redirect:"+url;
            }

        }else{
            return "redirect:"+url;
        }
    }
    @GetMapping("/logout")
    public String logout(HttpServletRequest request,HttpServletResponse response){
        request.getSession().removeAttribute("user");
        Cookie cookie = new Cookie("token",null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/";

    }

}
