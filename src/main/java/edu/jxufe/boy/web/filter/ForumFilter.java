package edu.jxufe.boy.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import edu.jxufe.boy.entity.User;
import org.apache.commons.lang.StringUtils;

import static edu.jxufe.boy.cons.CommonConstant.LOGIN_TO_URL;
import static edu.jxufe.boy.cons.CommonConstant.USER_CONTEXT;


public class ForumFilter implements Filter {
	private static final String FILTERED_REQUEST = "@@session_context_filtered_request";

//	// ① 不需要登录即可访问的URI资源
//	private static final String[] INHERENT_ESCAPE_URIS = { "/index.jsp",
//			"/index.html", "/login.jsp", "/login/doLogin.html",
//			"/register.jsp", "/register.html", "/board/listBoardTopics-",
//			"/board/listTopicPosts-" };

	// ① 不需要登录即可访问的URI资源
	private static final String[] INHERENT_ESCAPE_URIS = {"/homepage","/RegisterController","/login","/BoardManage/AllBoards","/BoardManage/board/loadBoardTopicsPage-","/BoardManage/board/listBoardTopics-",
			"/BoardManage/board/loadTopicPostPage-","/BoardManage/board/listTopicPosts-"};

	private static final String[] MANAGER_URIS = {"/ForumManage"};
	// ② 执行过滤
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;

		// ②-1 保证该过滤器在一次请求中只被调用一次
		if (request != null && request.getAttribute(FILTERED_REQUEST) != null) {
			chain.doFilter(request, response);
		} else {
			String a = httpRequest.getRequestURI();
 			if(a.contains(".css") || a.contains(".js") || a.contains(".png")|| a.contains(".jpg")||a.contains(".ico")||a.contains(".ttf")||a.contains(".woff"))
 			{
				//如果发现是css或者js文件，直接放行
				chain.doFilter(request, response);
			}else {

				// ②-2 设置过滤标识，防止一次请求多次过滤
				request.setAttribute(FILTERED_REQUEST, Boolean.TRUE);

				User userContext = getSessionUser(httpRequest);

				//如果当前用户不是管理员并且访问uri是管理页面
				if(userContext!=null) {
					if (userContext.getUserType()!=2 && isManagerURI(httpRequest.getRequestURI(), httpRequest)) {
						request.getRequestDispatcher("/homepage").forward(request,
								response);
						return;
					}
				}
				// ②-3 用户未登录, 且当前URI资源需要登录才能访问
				if (userContext == null
						&& !isURILogin(httpRequest.getRequestURI(), httpRequest)) {
					String toUrl = httpRequest.getRequestURL().toString();
					if (!StringUtils.isEmpty(httpRequest.getQueryString())) {
						toUrl += "?" + httpRequest.getQueryString();
					}
					// 将用户的请求URL保存在session中，用于登录成功之后，跳到目标URL
					httpRequest.getSession().setAttribute(LOGIN_TO_URL, toUrl);
					// ②-5转发到登录页面
					request.getRequestDispatcher("/homepage/toLogin").forward(request,
							response);
					return;
				}
				chain.doFilter(request, response);
			}
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}
   /**
    * 当前URI资源是否需要登录才能访问
    * @param requestURI
    * @param request
    * @return
    */
	private boolean isURILogin(String requestURI, HttpServletRequest request) {
		String stringURI = request.getContextPath();
		if (request.getContextPath().equalsIgnoreCase(requestURI)
				|| (request.getContextPath() + "/").equalsIgnoreCase(requestURI))
			return true;
		for (String uri : INHERENT_ESCAPE_URIS) {
			if (requestURI != null && requestURI.indexOf(uri) >= 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 当前URI资源需要管理员登陆才能访问
	 * @param requestURI
	 * @param request
	 * @return
	 */
	private boolean isManagerURI(String requestURI, HttpServletRequest request){
		if (request.getContextPath().equalsIgnoreCase(requestURI)
				|| (request.getContextPath() + "/").equalsIgnoreCase(requestURI))
			return true;
		for (String uri : MANAGER_URIS) {
			if (requestURI != null && requestURI.indexOf(uri) >= 0) {
				return true;
			}
		}
		return false;
	}

	protected User getSessionUser(HttpServletRequest request) {
		return (User) request.getSession().getAttribute(USER_CONTEXT);
	}

	@Override
	public void destroy() {

	}
}
