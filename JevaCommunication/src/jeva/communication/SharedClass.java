package jeva.communication;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SharedClass
{
	SharePolicy policy();

	String name();
}
