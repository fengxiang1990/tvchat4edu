package com.bizcom.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.support.v4.util.LruCache;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.UnderlineSpan;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bizcom.vc.widget.cus.span.ClickableImageSpan;
import com.bizcom.vc.widget.cus.span.ClickableLinkSpan;
import com.bizcom.vo.FileInfoBean;
import com.bizcom.vo.User;
import com.bizcom.vo.meesage.VMessage;
import com.bizcom.vo.meesage.VMessageAbstractItem;
import com.bizcom.vo.meesage.VMessageAudioItem;
import com.bizcom.vo.meesage.VMessageFaceItem;
import com.bizcom.vo.meesage.VMessageFileItem;
import com.bizcom.vo.meesage.VMessageImageItem;
import com.bizcom.vo.meesage.VMessageLinkTextItem;
import com.bizcom.vo.meesage.VMessageTextItem;
import com.config.GlobalConfig;
import com.config.GlobalHolder;
import com.shdx.tvchat.phone.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {

	public static final int MESSAGE_TYPE_IMAGE = 0;
	public static final int MESSAGE_TYPE_AUDIO = 1;

	private static LruCache<String, Bitmap> BitmapCache = new LruCache<String, Bitmap>(
			(int) ((Runtime.getRuntime().maxMemory()) / 8)) {

		@Override
		protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
			if (key != null) {
				if (BitmapCache != null) {
					Bitmap bm = BitmapCache.remove(key);
					if (bm != null && !bm.isRecycled()) {
						bm.recycle();
					}
				}
			}
		}
	};

	/**
	 * Convert message content to comfortable content for conversation display
	 * if only send image: [pic] if send emoji and text: show text and emoji
	 * 
	 * @param context
	 * @param vm
	 * @return
	 */
	public static CharSequence getMixedConversationContent(Context context, VMessage vm) {
		if (vm == null || context == null) {
			return null;
		}
		SpannableStringBuilder builder = new SpannableStringBuilder();
		if (vm.getAudioItems().size() > 0) {
			builder.append(context.getResources().getText(R.string.conversation_display_item_audio));
			return builder;
		}

		if (vm.getFileItems().size() > 0) {
			builder.append(context.getResources().getText(R.string.conversation_display_item_file));
			return builder;
		}

		// If no text and face item, now means send picture
		if (vm.getImageItems().size() > 0) {
			builder.append(context.getResources().getText(R.string.conversation_display_item_pic));
			return builder;
		}

		for (int i = 0; i < vm.getItems().size(); i++) {
			VMessageAbstractItem item = vm.getItems().get(i);
			if (item.getType() == VMessageAbstractItem.ITEM_TYPE_TEXT) {
				VMessageTextItem textItem = (VMessageTextItem) item;
				if (!TextUtils.isEmpty(textItem.getText()))
					builder.append(textItem.getText()).append(" ");
			} else if (item.getType() == VMessageAbstractItem.ITEM_TYPE_LINK_TEXT) {
				builder.append(((VMessageLinkTextItem) item).getText()).append(" ");
			} else if (item.getType() == VMessageAbstractItem.ITEM_TYPE_FACE) {
				Drawable dr = context.getResources()
						.getDrawable(GlobalConfig.GLOBAL_FACE_ARRAY[((VMessageFaceItem) item).getIndex()]);
				appendSpan(builder, dr, ((VMessageFaceItem) item).getIndex());
			}
		}
		return builder;
	}

	/**
	 * Get text content from message. convert face item to special symbol
	 * 
	 * @param vm
	 * @return
	 * 
	 * @see GlobalConfig#getEmojiStrByIndex(int)
	 */
	public static CharSequence getMixedConversationCopyedContent(VMessage vm) {
		if (vm == null) {
			return null;
		}

		SpannableStringBuilder builder = new SpannableStringBuilder();
		for (int i = 0; i < vm.getItems().size(); i++) {
			VMessageAbstractItem item = vm.getItems().get(i);
			if (builder.length() != 0 && item.isNewLine()) {
				builder.append("\n");
			}
			if (item.getType() == VMessageAbstractItem.ITEM_TYPE_TEXT) {
				VMessageTextItem textItem = (VMessageTextItem) item;
				if (!TextUtils.isEmpty(textItem.getText()))
					builder.append(textItem.getText());
			} else if (item.getType() == VMessageAbstractItem.ITEM_TYPE_FACE) {
				builder.append(GlobalConfig.getEmojiStrByIndex(((VMessageFaceItem) item).getIndex()));
			} else if (item.getType() == VMessageAbstractItem.ITEM_TYPE_LINK_TEXT) {
				builder.append(((VMessageLinkTextItem) item).getText());
			}
		}

		return builder;
	}

	public static void appendSpan(SpannableStringBuilder builder, Drawable drw, int index) {
		if (builder == null || drw == null) {
			return;
		}

		drw.setBounds(0, 0, drw.getIntrinsicWidth(), drw.getIntrinsicHeight());
		String emoji = GlobalConfig.getEmojiStrByIndex(index);
		builder.append(emoji);

		ImageSpan is = new ImageSpan(drw, index + "");
		builder.setSpan(is, builder.length() - emoji.length(), builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}

	public static void buildChatPasteMessageContent(Context mContext, EditText mMessageET) {
		Editable edit = mMessageET.getEditableText();
		int num = 0;
		int flagCount = 0;
		String[] split = edit.toString().split("/:");
		for (String string : split) {
			if (string.contains(":")) {
				num++;
			}
		}
		if (num > 10 && split.length > 10) {
			Toast.makeText(mContext, R.string.error_contact_message_face_too_much, Toast.LENGTH_SHORT).show();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < split.length; i++) {

				if (flagCount == 10) {
					flagCount = 0;
					break;
				}

				if (split[i].contains(":")) {
					flagCount++;
					sb.append("/:");
					if (flagCount == 10 && split[i].split(" ").length > 1) {
						sb.append(split[i].split(" ")[0]);
					} else
						sb.append(split[i]);
				} else {
					sb.append(split[i]);
				}

			}
			edit.clear();
			edit.append(sb.toString().trim());
			mMessageET.setSelection(sb.toString().trim().length());
			sb.delete(0, sb.length());
		}

		int start = -1, end;
		int index = 0;
		while (index < edit.length()) {
			if (edit.charAt(index) == '/' && index < edit.length() - 1 && edit.charAt(index + 1) == ':') {
				start = index;
				index += 2;
				continue;
			} else if (start != -1) {
				if (edit.charAt(index) == ':' && index < edit.length() - 1 && edit.charAt(index + 1) == '/') {
					end = index + 2;
					SpannableStringBuilder builder = new SpannableStringBuilder();

					int ind = GlobalConfig.getDrawableIndexByEmoji(edit.subSequence(start, end).toString());
					// replay emoji and clean
					if (ind > 0) {
						MessageUtil.appendSpan(builder,
								mContext.getResources().getDrawable(GlobalConfig.GLOBAL_FACE_ARRAY[ind]), ind);
						edit.replace(start, end, builder);
					}
					index = start;
					start = -1;
				}
			}
			index++;
		}
	}

	public static VMessage buildChatMessage(Context mContext, String content, int groupType, long remoteGroupID,
			User remoteUser) {
		if (content.equals("")) {
			Toast.makeText(mContext, R.string.util_message_toast_error, Toast.LENGTH_SHORT).show();
			return null;
		}

		content = removeEmoji(content);

		VMessage vm = new VMessage(groupType, remoteGroupID, GlobalHolder.getInstance().getCurrentUser(), remoteUser,
				new Date(GlobalConfig.getGlobalServerTime()));
		String[] array;
		if (content.endsWith("\n")) {
			String[] split = content.split("\n");
			array = new String[split.length + 1];
			for (int i = 0; i <= split.length; i++) {
				if (i == split.length) {
					array[i] = "\n";
				} else {
					array[i] = split[i];
				}
			}
		} else {
			array = content.split("\n");
		}

		for (int i = 0; i < array.length; i++) {
			String str = array[i];
			int len = str.length();
			if (str.length() <= 4) {
				VMessageAbstractItem vai = new VMessageTextItem(vm, str);
				vai.setNewLine(true);
				continue;
			}

			int emojiStart = -1, end, strStart = 0;
			int index = 0;
			Pattern pattern = Pattern.compile(
					"(http://|https://|www\\.){1}[^\u4e00-\u9fa5\\s]*?\\.(com|net|cn|me|tw|fr|html){1}(/[^\u4e00-\u9fa5\\s]*){0,1}");
			while (index < str.length()) {
				if (str.charAt(index) == '/' && index < len - 1 && str.charAt(index + 1) == ':') {
					emojiStart = index;
					index += 2;
					continue;
				} else if (emojiStart != -1) {
					// Found end flag of emoji
					if (str.charAt(index) == ':' && index < len - 1 && str.charAt(index + 1) == '/') {
						end = index + 2;

						// If emojiStart lesser than strStart,
						// mean there exist string before emoji
						if (strStart < emojiStart) {
							String strTextContent = str.substring(strStart, emojiStart);
							VMessageTextItem vti = new VMessageTextItem(vm, strTextContent);
							// If strStart is 0 means string at new line
							if (strStart == 0) {
								vti.setNewLine(true);
							}

						}

						int ind = GlobalConfig.getDrawableIndexByEmoji(str.subSequence(emojiStart, end).toString());
						if (ind > 0) {
							// new face item and sendFriendToTv list
							VMessageFaceItem vfi = new VMessageFaceItem(vm, ind);
							// If emojiStart is 0 means emoji at new line
							if (emojiStart == 0) {
								vfi.setNewLine(true);
							}

						}
						// Assign end to index -1, do not assign end because
						// index will be ++
						index = end - 1;
						strStart = end;
						emojiStart = -1;
					}
				}

				int lastEnd = 0;
				boolean firstMather = true;
				// check if exist last string
				if (index == len - 1 && strStart <= index) {
					String strTextContent = str.substring(strStart, len);
					Matcher matcher = pattern.matcher(strTextContent);
					while (matcher.find()) {
						String url = matcher.group(0);
						V2Log.e("ConversationP2PTextActivity", "从文本内容检测到网址：" + url);
						// 检测网址前面是否有文本内容
						if (firstMather) {
							firstMather = false;
							if (matcher.start(0) != strStart) {
								VMessageTextItem vti = new VMessageTextItem(vm,
										strTextContent.substring(strStart, matcher.start(0)));
								// If strStart is 0 means string at new line
								if (strStart == 0) {
									vti.setNewLine(true);
								}
							}
                            VMessageLinkTextItem linkItem = new VMessageLinkTextItem(vm, url, url);
                            // If strStart is 0 means string at new line
                            if (matcher.start(0) == strStart && matcher.start(0) == 0) {
                                linkItem.setNewLine(true);
                            }
						} else {
							if (matcher.start(0) != lastEnd) {
								VMessageTextItem vti = new VMessageTextItem(vm,
										strTextContent.substring(lastEnd, matcher.start(0)));
								// If strStart is 0 means string at new line
								if (lastEnd + 1 == 0) {
									vti.setNewLine(true);
								}
							}

                            VMessageLinkTextItem linkItem = new VMessageLinkTextItem(vm, url, url);
                            // If strStart is 0 means string at new line
                            if (matcher.start(0) == strStart && strStart == 0) {
                                linkItem.setNewLine(true);
                            }
						}
						lastEnd = matcher.end(0);
					}

					if (strTextContent.length() != lastEnd) {
						String lastText = strTextContent.substring(lastEnd, strTextContent.length());
						VMessageTextItem vti;
						if ("\n".equals(lastText)) {
							vti = new VMessageTextItem(vm, "");
						} else {
							vti = new VMessageTextItem(vm, lastText);
							if (str.equals(lastText)) {
								vti.setNewLine(true);
							} else {
								if (!str.contains(lastText)) {
									vti.setNewLine(true);
								}
							}
						}
					}
					strStart = index;
				}
				index++;
			}
		}
		return vm;
	}

	/**
	 * 构建消息TextView
	 * 
	 * @param mContext
	 * @param mMsgBodyTV
	 * @param vm
	 */
	public static TextView buildChatTextView(final Context mContext, final TextView mMsgBodyTV, final VMessage vm,
			final ChatTextViewClick callback) {
		List<VMessageAbstractItem> items = vm.getItems();
		for (int i = 0; items != null && i < items.size(); i++) {
			VMessageAbstractItem item = items.get(i);
			// Add new layout for new line
			if (item.isNewLine() && mMsgBodyTV.length() != 0) {
				mMsgBodyTV.append("\n");
			}

			if (i == 0 && vm.isAutoReply()) {
				mMsgBodyTV.append(mContext.getResources().getString(R.string.contact_message_auto_reply));
			}

			if (item.getType() == VMessageAbstractItem.ITEM_TYPE_TEXT) {
				VMessageTextItem textItem = (VMessageTextItem) item;
				if (!TextUtils.isEmpty(textItem.getText()))
					mMsgBodyTV.append(textItem.getText());
			} else if (item.getType() == VMessageAbstractItem.ITEM_TYPE_FACE) {
				Drawable dr = mContext.getResources()
						.getDrawable(GlobalConfig.GLOBAL_FACE_ARRAY[((VMessageFaceItem) item).getIndex()]);
				dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());

				SpannableStringBuilder builder = new SpannableStringBuilder("v2tech");
				ImageSpan is = new ImageSpan(dr);
				builder.setSpan(is, 0, "v2tech".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				mMsgBodyTV.append(builder);
			} else if (item.getType() == VMessageAbstractItem.ITEM_TYPE_IMAGE) {
				final VMessageImageItem image = (VMessageImageItem) item;
				final String filePath = image.getFilePath();

				if (filePath.equals("wait")) {
					Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),
							R.drawable.ws_receive_image_wait);
					startFlushImage(mContext, mMsgBodyTV, image, bitmap, callback);
					continue;
				}

				if (filePath.equals("error")) {
					Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),
							R.drawable.ws_download_error_icon);
					startFlushImage(mContext, mMsgBodyTV, image, bitmap, callback);
					continue;
				}

				// if (!waitQueue.contains(filePath)) {
				final Bitmap bitmap = BitmapCache.get(image.getFilePath());
				if (bitmap != null && !bitmap.isRecycled()) {
					startFlushImage(mContext, mMsgBodyTV, image, bitmap, callback);
					// callback.imageFrush(mMsgBodyTV, image, bitmap ,
					// callback);
				} else {
					Bitmap hurtBitmap = BitmapUtil.getCompressedBitmap(filePath);
					BitmapCache.put(filePath, hurtBitmap);
					startFlushImage(mContext, mMsgBodyTV, image, hurtBitmap, callback);
					// waitQueue.sendFriendToTv(filePath);
					// Tasks.executeInBackground(mContext,
					// new BackgroundWork<Bitmap>() {
					//
					// @Override
					// public Bitmap doInBackground()
					// throws Exception {
					// return BitmapUtil.getCompressedBitmap(filePath);
					// }
					// }, new Completion<Bitmap>() {
					//
					// @Override
					// public void onSuccess(Context context,
					// Bitmap result) {
					// waitQueue.remove(filePath);
					// BitmapCache.put(filePath, result);
					//// startFlushImage(mContext , mMsgBodyTV, image, result ,
					// callback);
					// callback.imageFrush(mMsgBodyTV, image, bitmap ,
					// callback);
					// }
					//
					// @Override
					// public void onError(Context context,
					// Exception e) {
					//
					// }
					// });
				}
				// }
				// AudioItem only has one item
			} else if (item.getType() == VMessageAbstractItem.ITEM_TYPE_LINK_TEXT) {
				VMessageLinkTextItem link = (VMessageLinkTextItem) item;
				String linkText = link.getText();
				SpannableStringBuilder style = new SpannableStringBuilder(((VMessageLinkTextItem) item).getText());
				ClickableLinkSpan is = new ClickableLinkSpan(link) {

					@Override
					public void onClick(VMessageLinkTextItem link) {
						callback.LinkItemClick(link);
					}
				};

				style.setSpan(is, 0, linkText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				style.setSpan(new ForegroundColorSpan(Color.BLUE), 0, linkText.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				style.setSpan(new UnderlineSpan(), 0, linkText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				mMsgBodyTV.append(style);
			}
		}
		return mMsgBodyTV;
	}

	/**
	 * 刷新mMsgBodyTV中的图片
	 * 
	 * @param mContext
	 * @param mMsgBodyTV
	 * @param imageItem
	 * @param result
	 * @param imageItemCallBack
	 */
	private static void startFlushImage(Context mContext, TextView mMsgBodyTV, VMessageImageItem imageItem,
			Bitmap result, final ChatTextViewClick imageItemCallBack) {

		String content = " ";
		mMsgBodyTV.append(content);
		Drawable dr = new BitmapDrawable(mContext.getResources(), result);
		dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
		ClickableImageSpan is = new ClickableImageSpan(dr, imageItem) {

			@Override
			public void onClick(VMessageImageItem imageItem) {
				imageItemCallBack.imageItemClick(imageItem);
			}
		};

		SpannableStringBuilder builder = new SpannableStringBuilder("v2tech");
		builder.setSpan(is, 0, "v2tech".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		mMsgBodyTV.append(builder);
	}

	public static VMessage buildAudioMessage(int groupType, long groupID, User fromUser, User toUser, String audioPath,
			int seconds) {
		VMessage vm = new VMessage(groupType, groupID, fromUser, toUser, new Date(GlobalConfig.getGlobalServerTime()));
		String uuid = audioPath.substring(audioPath.lastIndexOf("/") + 1, audioPath.lastIndexOf("."));
		VMessageAudioItem item = new VMessageAudioItem(vm, uuid, audioPath, seconds, VMessageAbstractItem.STATE_READED);
		item.setState(VMessageAbstractItem.TRANS_TRANSING);
		return item.getVm();
	}

	public static VMessage buildFileMessage(int groupType, long groupID, User fromUser, User toUser,
			FileInfoBean bean) {
		VMessage vm = new VMessage(groupType, groupID, fromUser, toUser, new Date(GlobalConfig.getGlobalServerTime()));
		VMessageFileItem item = new VMessageFileItem(vm, bean.filePath, VMessageFileItem.STATE_FILE_SENDING);
		return item.getVm();
	}

	public static VMessage buildImageMessage(int groupType, long groupID, User fromUser, User toUser,
			String imagePath) {
		String uuid = UUID.randomUUID().toString();
		File newFile = copyBinaryData(MESSAGE_TYPE_IMAGE, imagePath, uuid);
		if (newFile == null)
			return null;
		imagePath = newFile.getAbsolutePath();
		VMessage vm = new VMessage(groupType, groupID, fromUser, toUser, new Date(GlobalConfig.getGlobalServerTime()));
		VMessageImageItem item = new VMessageImageItem(vm, uuid, imagePath, 0);
		item.setState(VMessageAbstractItem.TRANS_TRANSING);
		return item.getVm();
	}

	/**
	 * 将二进制数据拷贝到用户自身目录下存储
	 * 
	 * @param filePath
	 * @return
	 */
	static File copyBinaryData(int type, String filePath, String uuid) {

		if (TextUtils.isEmpty(filePath))
			return null;

		String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
		File srcFile = new File(filePath);
		File desFile;
		if (!srcFile.exists())
			return null;

		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(srcFile);
			switch (type) {
			case MESSAGE_TYPE_IMAGE:
				desFile = new File(
						GlobalConfig.getGlobalPicsPath() + "/" + uuid + fileName.substring(fileName.lastIndexOf(".")));
				break;
			case MESSAGE_TYPE_AUDIO:
				desFile = new File(
						GlobalConfig.getGlobalAudioPath() + "/" + uuid + fileName.substring(fileName.lastIndexOf(".")));
				break;
			default:
				throw new RuntimeException("the copy binary was wroing , unknow type :" + type);
			}

			if (filePath.equals(desFile.getAbsolutePath())) {
				return srcFile;
			}

			fos = new FileOutputStream(desFile);
			int len = 0;
			byte[] buf = new byte[1024];
			while ((len = fis.read(buf)) != -1) {
				fos.write(buf, 0, len);
				fos.flush();
			}
			return desFile;
		} catch (Exception e) {
			V2Log.e(e.getLocalizedMessage());
			return null;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					V2Log.e("MessageBuilder copyBinaryData : the FileInputStream closed failed...");
					e.printStackTrace();
				}
			}

			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					V2Log.e("MessageBuilder copyBinaryData : the FileOutputStream closed failed...");
					e.printStackTrace();
				}
			}
		}
	}

	public static void clearLruCache() {
		BitmapCache.evictAll();
	}

	public static void hideKeyBoard(Context mContext, IBinder token) {
		if (token != null) {
			InputMethodManager manager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
			manager.hideSoftInputFromWindow(token, 0);
		}
	}

    public static void showKeyBoard( EditText token){
        token.setFocusable(true);
        token.setFocusableInTouchMode(true);
        token.requestFocus();
        InputMethodManager inputManager =
                (InputMethodManager)token.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(token, 0);
    }

	/**
	 * FIXME optimize code 去除IOS自带表情
	 * 
	 * @param content
	 * @return
	 */
	public static String removeEmoji(String content) {
		byte[] bys = new byte[] { -16, -97 };
		byte[] bc = content.getBytes();
		byte[] copy = new byte[bc.length];
		int j = 0;
		for (int i = 0; i < bc.length; i++) {
			if (i < bc.length - 2 && bys[0] == bc[i] && bys[1] == bc[i + 1]) {
				i += 3;
				continue;
			}
			copy[j] = bc[i];
			j++;
		}
		return new String(copy, 0, j);
	}

	/**
	 * 半角转换为全角
	 * 
	 * @param input
	 * @return
	 */
	public static String ToDBC(String input) {
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 12288) {
				c[i] = (char) 32;
				continue;
			}
			if (c[i] > 65280 && c[i] < 65375)
				c[i] = (char) (c[i] - 65248);
		}
		return new String(c);
	}

	/**
	 * 去除特殊字符或将所有中文标号替换为英文标号
	 * 
	 * @param str
	 * @return
	 */
	public static String stringFilter(String str) {
		str = str.replaceAll("【", "[").replaceAll("】", "]").replaceAll("！", "!").replaceAll("：", ":").replaceAll(" ",
				"");// 替换中文标号
		String regEx = "[『』]"; // 清除掉特殊字符
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		return m.replaceAll("").trim();
	}

	/**
	 * 判断是否为手机号
	 * 
	 * @param mobiles
	 * @return
	 */
	public static boolean isMobileNO(String mobiles) {
		Pattern p = Pattern.compile("[1][358]\\d{9}");
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}

	public static boolean checkIfNum(String s) {
		try {
			int i = Integer.parseInt(s);

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 获取首字母
	 * @return
	 */
	public static String getFirstLetterName(String s){
		String name ="";
		if(!TextUtils.isEmpty(s)){
			int end = 1;
			if(checkIfNum(s)){
				end=2;
			}
			if(s.length()>=2){
				name= s.substring(0, end);
			}else{
				name = s;
			}

		}
		return name;
	}

	public interface ChatTextViewClick {

		void imageItemClick(VMessageImageItem imageItem);

		void LinkItemClick(VMessageLinkTextItem link);

		void imageFrush(TextView mMsgBodyTV, VMessageImageItem imageItem, Bitmap result,
				ChatTextViewClick imageItemCallBack);
	}
}
