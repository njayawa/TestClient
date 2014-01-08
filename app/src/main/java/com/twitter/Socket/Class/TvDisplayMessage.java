package com.twitter.Socket.Class;

/**
 * Created by nj on 1/8/14.
 * Displays message on TV screen
 */
public class TvDisplayMessage {
    public static String MESSAGE = "DISPLAY_MESSAGE";
    public String Caption;
    public String Text;
    public String[] Buttons; //array of buttons to display.  default is OK
    public String DurationSeconds; //how long to display the message default is 2 seconds
}
