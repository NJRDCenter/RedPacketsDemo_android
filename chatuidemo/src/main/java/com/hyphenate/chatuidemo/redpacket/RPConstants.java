package com.hyphenate.chatuidemo.redpacket;

/**
 * 红包的一些常量定义
 *
 * Created by czhang on 16/11/21.
 */
public class RPConstants {

	/**
	 * 是否为红包
	 */
	public static final String MESSAGE_ATTR_IS_RED_PACKET_MESSAGE = "is_red_packet_message";
	/**
	 * 红包id
	 */
	public static final String MESSAGE_ATTR_REDPKG_ID = "redpkg_id";
	/**
	 * 红包发送者用户id
	 */
	public static final String MESSAGE_ATTR_SENDER_USERID = "sender_user_id";
	/**
	 * 红包类型
	 */
	public static final String MESSAGE_ATTR_REDPKG_TYPE = "redpkg_type";
	/**
	 * 红包描述
	 */
	public static final String MESSAGE_ATTR_REDPKG_DESC = "redpkg_desc";

	/**
	 * 是否为接收红包信息
	 */
	public static final String MESSAGE_ATTR_REV_IS_RED_PACKET_MESSAGE = "rev_is_red_packet_message";
	/**
	 * 红包发送者用户id
	 */
	public static final String MESSAGE_ATTR_REV_SENDER_USERID = "rev_sender_user_id";
	/**
	 * 红包接收者用户id
	 */
	public static final String MESSAGE_ATTR_REV_RECEIVER_USERID = "rev_receiver_user_id";

}
