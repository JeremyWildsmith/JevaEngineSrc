/*******************************************************************************
 * Copyright (c) 2013 Jeremy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * If you'd like to obtain a another license to this code, you may contact Jeremy to discuss alternative redistribution options.
 * 
 * Contributors:
 *     Jeremy - initial API and implementation
 ******************************************************************************/
package io.github.jevaengine.config;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableStore extends BasicVariable
{

	private static final Pattern REGEX_VAR = Pattern.compile("\\s*(?<name>((/?([^/:\r\n]+)))+):(?<value>[^;]*);");

	private static final Pattern REGEX_COMMENT = Pattern.compile("/\\*[\\x00-\\xFF]*?(?=\\*/)\\*/|//[^\\r\\n]*");

	private static final String GROUP_NAME = "name";

	private static final String GROUP_VALUE = "value";

	public VariableStore()
	{
		super();
	}

	public static VariableStore create(InputStream srcStream)
	{
		VariableStore varStore = new VariableStore();

		Scanner scanner = new Scanner(srcStream, "UTF-8");

		scanner.useDelimiter("\\A");

		String contents = (scanner.hasNext() ? scanner.next() : "");

		scanner.close();

		String variablesCommentless = REGEX_COMMENT.matcher(contents).replaceAll("");

		Matcher matcher = REGEX_VAR.matcher(variablesCommentless);

		while (matcher.find())
		{
			varStore.setVariable(matcher.group(GROUP_NAME), new VariableValue(decodeRaw(matcher.group(GROUP_VALUE))));
		}

		return varStore;
	}

	private static String decodeRaw(String raw)
	{
		try
		{
			return URLDecoder.decode(raw, "ISO-8859-1");
		} catch (UnsupportedEncodingException e)
		{
			throw new RuntimeException(e);
		}
	}
}
