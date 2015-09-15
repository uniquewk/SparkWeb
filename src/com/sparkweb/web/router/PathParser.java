package com.sparkweb.web.router;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 核心的路由路径转换解析器
 * 
 * @author yswang
 * @version 1.0
 */
class PathParser
{
	/**
	 * 将一个含有正则表达式或命名参数(:key)的路径字符串转换为可以进行正则匹配的正则对象。
	 * <br>
	 * 例如：/index == \/index\/? <br>
	 * /index/(\\d+) == \/index\/(\d+)\/? <br>
	 * /index/:id == \/index\/(?:([^\/]+?))\/? <br>
	 * /index/:id? == \/index(?:\/([^\/]+?))?\/? <br>
	 * /index/:id(\\d+) == \/index\/(?:(\d+))\/? <br>
	 * 
	 * @param path 路径
	 * @param keys 指定存储命名参数的容器
	 * @param sensitive 是否要求大小写敏感，默认 false 不区分大小写
	 * @param strict 是否启动严格的路径匹配模式，默认 false. 严格模式：/A != /A/、非严格模式：/A == /A/
	 * @return
	 */
	static Pattern parseRoutePathRegexp(final String path, final List<NamedKey> keys, 
			final boolean sensitive, final boolean strict)
	{
		String _pathRegexp = path;
		
		if(!strict && !path.endsWith("/?")) {
			// solve: \/a\/\/? --> \/a\/?
			_pathRegexp += _pathRegexp.charAt(_pathRegexp.length() - 1) == '/' ? "?" : "/?";
		}
		
		// 把下面的处理取消：下面的代码将路径中含有的正则变量取消捕获了，导致无法获取正则变量值
		// 比如：/user/(\\d+)，无法将 /user/200 的200获取
		//_path = _path.replaceAll("\\/\\(", "(?:/");
		_pathRegexp = replaceAll(_pathRegexp, "(\\/)?(\\.)?:(\\w+)(?:(\\(.*?\\)))?(\\?)?(\\*)?", new ReplaceCallback() {
			@Override
			public String replace(String text, int index, Matcher m)
			{
				String slash = m.group(1);	// 是否有斜杠 /
				String format = m.group(2);	// 是否有点.
				String key = m.group(3); 	 // 命名参数名称
				String capture = m.group(4); // 命名参数后面使用的正则验证，比如：/user/:userid(\\d+) --> capture = (\d+)
				String optional = m.group(5); // 当前参数是否是可选的
				String star = m.group(6);	  // 是否有星号通配符匹配
				
				if(keys != null) {
					keys.add(new NamedKey(key, optional != null));
				}
				
				if(slash == null) {
					slash = "";
				}
				
				return ""
						+ (optional != null ? "" : slash)
						+ "(?:"
						+ (optional != null ? slash : "")
						+ (format != null ? format : "")
						// 对 capture的正则\进行转义，使得在新的正则路径中有效：capture = (\d+) --> (\\d+)
						//+ (capture != null ? capture : (format != null ? "([^/.]+?)" : "([^/]+?)")) + ")"
						+ (capture != null ? capture.replace("\\", "\\\\") : (format != null ? "([^/.]+?)" : "([^/]+?)")) + ")"
						+ (optional != null ? optional : "")
						+ (star != null ? "(/*)?" : "");
			}
		});
		
		_pathRegexp = _pathRegexp.replaceAll("([\\/.])", "\\\\$1").replaceAll("\\*", "(.*)");
		
		// 下面这个处理过程很重要，是为了能够完美支持命名参数和正则参数的混合配置！
		// eg: /user/:id/:bid/(\\d+), /user/:id/(\\d+)/:bid
		if(keys != null && !keys.isEmpty())
		{
			// 提取 _pathRegexp 中的分组信息，用于明确指定含有的命名参数在整个分组位置中所在的真实位置
			// 比如：/user/:id/:bid/(\\d+) --> id:处于第1个参数位置，bid:处于第2个参数位置
			// /user/:id/(\\d+)/:bid --> id:处于第1个参数位置，bid:处于第3个参数位置
			// 通过对分组的检测来调整 List<NamedKey> keys的命名name的索引位置，
			// 这样在 {@link Route#match} 中就能准确的处理命名参数和正则参数的数据索引位置
			
			Deque<Character> stack = new ArrayDeque<Character>();
			StringBuilder sb = new StringBuilder(20);
			// 记录分组参数索引，如果当前参数为命名参数，则索引的取值为1，否则为 0
			// eg: /users/(\\d+) --> [0] 1个参数
			//	   /users/:id/(\\d+) --> [1, 0] 2个参数
			List<Integer> paramIndexs = new ArrayList<Integer>();
			
			for(int i = 0, len = _pathRegexp.length(); i < len; ++i)
			{
				char c = _pathRegexp.charAt(i);
				// 待检测的 ( ) 是合法的正则符号，而不应该是转义的普通()字符
				boolean unEscaped = (i == 0 || _pathRegexp.charAt(i-1) != '\\');
				if(c == '(' && unEscaped)
				{
					stack.push(c);
					sb.append(c);
				}
				else if(c == ')' && unEscaped)
				{
					stack.pop();
					sb.append(c);
					
					// 每个分组()匹配完成出栈结束
					if(stack.size() == 0)
					{
						// 凡是正则串中含有 ?: 的，则认为这是一个命名参数所生成的正则串
						paramIndexs.add(sb.toString().indexOf("?:") != -1 ? 1 : 0);
						// 清除当前分组内容，用于下一次分组内容记录
						sb.delete(0, sb.length());
					}
				}
				else if(stack.size() > 0) {
					sb.append(c);
				}
			}
			
			// 转换后的正则分组出现了不匹配现象，这是一个错误的正则
			if(stack.size() > 0)
			{
				throw new IllegalStateException("The Path regexp <"+ _pathRegexp +"> has unmatched group!");
			}
			
			sb = null;
			stack.clear(); 
			stack = null;
			
			// 调整 List<NamedKey> keys 中已经保存的命名参数位置
			// 目前 keys 的命名参数是顺序存放的，不能正确体现其所在整个正则分组中的实际位置
			int c = 0; // 获取keys中原始保存的命名参数对象
			NamedKey[] namedKeys = keys.toArray(new NamedKey[keys.size()]);
			keys.clear();
			
			for(int j = 0, size = paramIndexs.size(); j < size; ++j)
			{
				keys.add((paramIndexs.get(j) == 1 && c < namedKeys.length) ? namedKeys[c++] : null);
			}
			
			namedKeys = null;
			paramIndexs.clear();
			paramIndexs = null;
		}
		
		return sensitive ? Pattern.compile(_pathRegexp) : Pattern.compile(_pathRegexp, Pattern.CASE_INSENSITIVE);
	}
	
	/**
	 * normalize file path
	 * 
	 * @param path
	 * @return
	 */
	static String normalizePath(String path)
	{
		if(path == null || path.trim().length() == 0)
		{
			return "";
		}
		
		String[] segments = splitOmitEmptyString(path, "/");
		boolean absolute = path.charAt(0) == '/';
		
		LinkedList<String> paths = new LinkedList<String>();
		
		for(String seg : segments)
		{
			if(".".equals(seg))
			{
				continue;
			}
			
			if("..".equals(seg))
			{
				if(paths.isEmpty() || "..".equals(paths.getLast())) 
				{
                    paths.add(seg);
                } else {
                    paths.removeLast();
                }
			} 
			else {
				paths.add(seg);
			}
		}
		
		StringBuilder sb = new StringBuilder(path.length());
		if(absolute)
		{
			sb.append('/');
		}
		
		int c = 0, last = paths.size() - 1;
		for(String seg : paths)
		{
			sb.append(seg);
			if(c++ < last)
			{
				sb.append('/');
			}
		}
		
		if(path.charAt(path.length() - 1) == '/')
		{
			sb.append('/');
		}
		
		return sb.toString().replaceAll("\\/{2,}", "/");
	}
	
	static String[] splitOmitEmptyString(String str, String delim)
	{
		if(str == null)
		{
			return new String[0];
		}
		
		String[] splits = str.split(delim);
		List<String> segs = new ArrayList<String>(splits.length);
		for(String seg : splits)
		{
			if(seg.trim().length() == 0)
			{
				continue;
			}
			
			segs.add(seg);
		}
		
		return segs.toArray(new String[segs.size()]);
	}
	
	/**
	 * 将String中的所有pattern匹配的字串替换掉
	 * 
	 * @param string 代替换的字符串
	 * @param pattern 替换查找的正则表达式对象
	 * @param replacement 替换函数
	 * @return
	 */
	private static String replaceAll(String str, String regexp, ReplaceCallback replacement)
	{
		if(str == null)
		{
			return null;
		}

		Matcher m = Pattern.compile(regexp).matcher(str);
		if(m.find())
		{
			StringBuffer sbuf = new StringBuffer();
			int index = 0;
			while(true)
			{
				m.appendReplacement(sbuf, replacement.replace(m.group(0), index++, m));
				if(!m.find())
				{
					break;
				}
			}

			m.appendTail(sbuf);

			return sbuf.toString();
		}

		return str;
	}

	static interface ReplaceCallback
	{
		/**
		 * 将text转化为特定的字串返回
		 * 
		 * @param text 指定的字符串
		 * @param index 替换的次序
		 * @param matcher Matcher对象
		 * @return
		 */
		String replace(String text, int index, Matcher matcher);
	}

}
