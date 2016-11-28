/* Chat Room ApprovedChars.java
 * EE422C Project 7 submission by
 * Regan Stehle
 * rms3762
 * 16465
 * Matthew Edwards
 * mwe295
 * 16475
 * Slip days used: <0>
 * Fall 2016
 */

package assignment7;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class ApprovedChars{
	public static Set<Character> approvedCharSet = new HashSet<Character>(
			Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
						  'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
						  'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G',
						  'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
						  'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3',
						  '4', '5', '6', '7', '8', '9', '0', '_', '-', '!', '@',
						  '#', '$', '%', '^', '&', '*', '(', ')', '+', '?', '.')
			);

}
