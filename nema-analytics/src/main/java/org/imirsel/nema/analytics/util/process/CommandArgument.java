/**
 * 
 */
package org.imirsel.nema.analytics.util.process;

public interface CommandArgument{
	public String toConfigString();
	public String toFormattedString();
	public boolean followedBySpace();
}