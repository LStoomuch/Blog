package edu.jxufe.boy.web.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.jxufe.boy.cons.CommonConstant;
import edu.jxufe.boy.entity.User;
import edu.jxufe.boy.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import static edu.jxufe.boy.cons.CommonConstant.LOGIN_TO_URL;

/**
 * 
 * <br>
 * <b>类描述:</b>
 * 
 * <pre>
 *   论坛管理，这部分功能由论坛管理员操作，包括：创建论坛版块、指定论坛版块管理员、
 * 用户锁定/解锁。
 * </pre>
 * 
 * @see
 * @since
 */
@Controller
@RequestMapping("/login")
public class LoginController extends BaseController {
	/**
	 * 自动注入
	 */
	@Autowired
	private UserService userService;
	
    /**
     * 用户登陆
     * @param request
     * @param user
     * @return
     */
	@RequestMapping("/doLogin")
	@ResponseBody
	public Map login(HttpServletRequest request, User user) {
		User dbUser = userService.getUserByUserName(user.getUserName());
		Map map = new HashMap();
		map.put("stat",1);
		if (dbUser == null) {
			map.put("stat",0);
			map.put("errorMsg", "用户名不存在");
		} else if (!dbUser.getPassword().equals(user.getPassword())) {
			map.put("stat",0);
			map.put("errorMsg", "用户密码不正确");
		} else if (dbUser.getLocked() == User.USER_LOCK) {
			map.put("stat",0);
			map.put("errorMsg", "用户已经被锁定，不能登录。");
		} else {
			dbUser.setLastIp(request.getRemoteAddr());
			dbUser.setLastVisit(new Date());
			userService.loginSuccess(dbUser);
			setSessionUser(request,dbUser);

			String toUrl = (String)request.getSession().getAttribute(CommonConstant.LOGIN_TO_URL);
			request.getSession().removeAttribute(CommonConstant.LOGIN_TO_URL);
			//如果当前会话中没有保存登录之前的请求URL，则直接跳转到主页
			if(StringUtils.isEmpty(toUrl)){
				toUrl = "/homepage";
			}
			map.put("toURL",toUrl);
		}
		return map;
	}

	/**
	 * 登录注销
	 * @param session
	 * @return
	 */
	@RequestMapping("/doLogout")
	public String logout(HttpSession session) {
		session.removeAttribute(CommonConstant.USER_CONTEXT);
		return "redirect:/homepage";
	}

}
