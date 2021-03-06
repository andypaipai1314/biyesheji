package com.wust.boyaBookStore.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.wust.boyaBookStore.exception.CustomException;
import com.wust.boyaBookStore.po.User;
import com.wust.boyaBookStore.service.UserService;
import cn.itcast.commons.CommonUtils;

/**
 * @ClassName UserController
 * @Description TODO(用户模块控制层)
 * @author hanyajun
 * @Date 2017年5月10日 下午8:01:16
 * @version 1.0.0
 */
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * ajax用户名是否注册校验
     * 
     * @param req
     * @param resp
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping("/ajaxValidateLoginname")

    public @ResponseBody boolean ajaxValidateLoginname(HttpServletRequest req, String loginname,
            HttpServletResponse resp) throws ServletException, IOException {

        /*
         * 1. 通过service得到校验结果
         */
        boolean b = false;
        try {
            b = userService.ajaxValidateLoginname(loginname);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        /*
         * 2. 发给客户端
         */
        return b;
    }

    /**
     * ajax Email是否注册校验
     * 
     * @param req
     * @param resp
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping("/ajaxValidateEmail")
    public @ResponseBody boolean ajaxValidateEmail(HttpServletRequest req, HttpServletResponse resp, String email)
            throws ServletException, IOException {

        /*
         * 1. 通过service得到校验结果
         */
        boolean b = false;
        try {
            b = userService.ajaxValidateEmail(email);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        /*
         * 2. 发给客户端
         */
        return b;
    }

    /**
     * ajax验证码是否正确校验
     * 
     * @param req
     * @param resp
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping("/ajaxValidateVerifyCode")
    public @ResponseBody boolean ajaxValidateVerifyCode(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        /*
         * 1. 获取输入框中的验证码
         */
        String verifyCode = req.getParameter("verifyCode");
        /*
         * 2. 获取图片上真实的校验码
         */
        String vcode = (String) req.getSession().getAttribute("vCode");
        /*
         * 3. 进行忽略大小写比较，得到结果
         */
        boolean b = verifyCode.equalsIgnoreCase(vcode);
        /*
         * 4. 发送给客户端
         */
        return b;
    }

    @RequestMapping("/jsps/user/ajaxValidateVerifyCode")
    public @ResponseBody boolean ajaxValidateVerifyCode2(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        /*
         * 1. 获取输入框中的验证码
         */
        String verifyCode = req.getParameter("verifyCode");
        /*
         * 2. 获取图片上真实的校验码
         */
        String vcode = (String) req.getSession().getAttribute("vCode");
        /*
         * 3. 进行忽略大小写比较，得到结果
         */
        boolean b = verifyCode.equalsIgnoreCase(vcode);
        /*
         * 4. 发送给客户端
         */
        return b;
    }

    /**
     * 注册功能
     * 
     * @param req
     * @param resp
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping("/regist")
    public String regist(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /*
         * 1. 封装表单数据到User对象
         */
        User formUser = CommonUtils.toBean(req.getParameterMap(), User.class);
        /*
         * 2. 校验之, 如果校验失败，保存错误信息，返回到regist.jsp显示
         */
        Map<String, String> errors = null;
        try {
            errors = validateRegist(formUser, req.getSession());
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        if (errors.size() > 0) {
            req.setAttribute("form", formUser);
            req.setAttribute("errors", errors);
            return "jsps/user/regist";
        }
        /*
         * 3. 使用service完成业务
         */
        try {
            userService.regist(formUser);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        /*
         * 4. 保存成功信息，转发到msg.jsp显示！
         */
        req.setAttribute("code", "success");
        req.setAttribute("msg", "注册成功，请马上到邮箱激活！");
        req.setAttribute("msgname1", "激活");
        req.setAttribute("msgname2", "登陆");
        String email = formUser.getEmail();
        String mailurl="http://mail."+email.substring(email.lastIndexOf("@")+1);
        req.setAttribute("msgurl1", mailurl);
        req.setAttribute("msgurl2", "/boyaBookStore/jsps/user/login.jsp");
        return "jsps/msg";
    }

    /*
     * 注册校验 对表单的字段进行逐个校验，如果有错误，使用当前字段名称为key，错误信息为value，保存到map中 返回map
     */
    private Map<String, String> validateRegist(User formUser, HttpSession session) throws Exception {
        Map<String, String> errors = new HashMap<String, String>();
        /*
         * 1. 校验登录名
         */
        String loginname = formUser.getLoginname();
        if (loginname == null || loginname.trim().isEmpty()) {
            errors.put("loginname", "用户名不能为空！");
        } else if (loginname.length() < 3 || loginname.length() > 20) {
            errors.put("loginname", "用户名长度必须在3~20之间！");
        } else if (!userService.ajaxValidateLoginname(loginname)) {
            errors.put("loginname", "用户名已被注册！");
        }

        /*
         * 2. 校验登录密码
         */
        String loginpass = formUser.getLoginpass();
        if (loginpass == null || loginpass.trim().isEmpty()) {
            errors.put("loginpass", "密码不能为空！");
        } else if (loginpass.length() < 3 || loginpass.length() > 20) {
            errors.put("loginpass", "密码长度必须在3~20之间！");
        }

        /*
         * 3. 确认密码校验
         */
        String reloginpass = formUser.getReloginpass();
        if (reloginpass == null || reloginpass.trim().isEmpty()) {
            errors.put("reloginpass", "确认密码不能为空！");
        } else if (!reloginpass.equals(loginpass)) {
            errors.put("reloginpass", "两次输入不一致！");
        }

        /*
         * 4. 校验email
         */
        String email = formUser.getEmail();
        if (email == null || email.trim().isEmpty()) {
            errors.put("email", "Email不能为空！");
        } else if (!email.matches("^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+((\\.[a-zA-Z0-9_-]{2,3}){1,2})$")) {
            errors.put("email", "Email格式错误！");
        } else if (!userService.ajaxValidateEmail(email)) {
            errors.put("email", "Email已被注册！");
        }

        /*
         * 5. 验证码校验
         */
        String verifyCode = formUser.getVerifyCode();
        String vcode = (String) session.getAttribute("vCode");
        if (verifyCode == null || verifyCode.trim().isEmpty()) {
            errors.put("verifyCode", "验证码不能为空！");
        } else if (!verifyCode.equalsIgnoreCase(vcode)) {
            errors.put("verifyCode", "验证码错误！");
        }

        return errors;
    }

    /**
     * 激活功能
     * 
     * @param req
     * @param resp
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping("/activation")
    public String activation(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /*
         * 1. 获取参数激活码 2. 用激活码调用service方法完成激活 > service方法有可能抛出异常, 把异常信息拿来，保存到request中，转发到msg.jsp显示 3.
         * 保存成功信息到request，转发到msg.jsp显示。
         */
        String code = req.getParameter("activationCode");
        try {
            userService.activatioin(code);
            req.setAttribute("code", "success");// 通知msg.jsp显示对号
            req.setAttribute("msg", "恭喜，激活成功，请马上登录！");
        } catch (CustomException e) {
            // 说明service抛出了异常
            req.setAttribute("msg", e.getMessage());
            req.setAttribute("code", "error");// 通知msg.jsp显示X
        }
        return "jsps/msg";
    }

    /**
     * 修改密码
     * 
     * @param req
     * @param resp
     * @return
     * @throws ServletException
     * @throws IOException
     */
    // 更新密码
    @RequestMapping(value = "/editpassword")
    public @ResponseBody boolean editpassword(HttpServletRequest req, String password) {
        User user = (User) req.getSession().getAttribute("sessionUser");
        userService.updatePassword2(user.getUid(), password);
        return true;
    }

    @RequestMapping("/updatePassword")
    public String updatePassword(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        /*
         * 1. 封装表单数据到user中 2. 从session中获取uid 3. 使用uid和表单中的oldPass和newPass来调用service方法 >
         * 如果出现异常，保存异常信息到request中，转发到pwd.jsp 4. 保存成功信息到rquest中 5. 转发到msg.jsp
         */
        User formUser = CommonUtils.toBean(req.getParameterMap(), User.class);
        User user = (User) req.getSession().getAttribute("sessionUser");
        // 如果用户没有登录，返回到登录页面，显示错误信息
        if (user == null) {
            req.setAttribute("msg", "您还没有登录！");
            return "jsps/user/login";
        }

        try {
            userService.updatePassword(user.getUid(), formUser.getNewpass(), formUser.getLoginpass());
            req.setAttribute("msg", "修改密码成功");
            req.setAttribute("code", "success");
            return "jsps/msg.jsp";
        } catch (CustomException e) {
            req.setAttribute("msg", e.getMessage());// 保存异常信息到request
            req.setAttribute("user", formUser);// 为了回显
            return "jsps/user/pwd";
        }
    }

    /**
     * 退出功能
     * 
     * @param req
     * @param resp
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping("/quit")
    public String quit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getSession().invalidate();
        return "redirect:first.action";
    }

    /**
     * 登录功能
     * 
     * @param req
     * @param resp
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping("/login")
    public ModelAndView login(HttpServletRequest req, HttpServletResponse resp, Model model)
            throws ServletException, IOException {
        /*
         * 1. 封装表单数据到User 2. 校验表单数据 3. 使用service查询，得到User 4. 查看用户是否存在，如果不存在： * 保存错误信息：用户名或密码错误 * 保存用户数据：为了回显 *
         * 转发到login.jsp 5. 如果存在，查看状态，如果状态为false： * 保存错误信息：您没有激活 * 保存表单数据：为了回显 * 转发到login.jsp 6. 登录成功： *
         * 保存当前查询出的user到session中 * 保存当前用户的名称到cookie中，注意中文需要编码处理。
         */
        /*
         * 1. 封装表单数据到user
         */
        User formUser = CommonUtils.toBean(req.getParameterMap(), User.class);

        /*
         * 3. 调用userService#login()方法
         */
        User user = null;
        try {
            user = userService.login(formUser);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        /*
         * 4. 开始判断
         */
        if (user == null) {
            model.addAttribute("msg", "用户名或密码错误！");
            model.addAttribute("formUser", formUser);
            model.addAttribute("url2", "/boyaBookStore/jsps/user/login.jsp");
            return new ModelAndView("redirect:/loginfirst.action");
        } else {
            if (!user.getStatus()) {
                model.addAttribute("msg", "您还没有激活！");
                model.addAttribute("formUser", formUser);
                model.addAttribute("url2", "/boyaBookStore/jsps/user/login.jsp");
                return new ModelAndView("redirect:/loginfirst.action");
            } else {
                // 保存用户到session
                req.getSession().setAttribute("sessionUser", user);
                // 获取用户名保存到cookie中
                String loginname = user.getLoginname();
                loginname = URLEncoder.encode(loginname, "utf-8");
                Cookie cookie = new Cookie("loginname", loginname);
                cookie.setMaxAge(60 * 60 * 24 * 10);// 保存10天
                resp.addCookie(cookie);
                model.addAttribute("msg", "登陆成功！");
                return new ModelAndView("redirect:/first.action");// 重定向到主页
            }
        }
    }

}
