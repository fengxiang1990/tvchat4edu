package com.bizcom.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import com.bizcom.vo.meesage.VMessageFileItem.FileType;
import com.config.GlobalConfig;
import com.shdx.tvchat.phone.R;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore.Images.ImageColumns;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * 用于打开文件的Utils
 * 
 * @author
 * 
 */
public class FileUtils {

	public static int adapterFileIcon(String fileName) {
		FileType fileType;
		if (fileName.indexOf(".") == -1)
			fileType = FileType.UNKNOW;
		else {
			String postfixName = fileName.substring(fileName.indexOf("."));
			fileType = getFileType(postfixName);
		}
		return adapterFileIcon(fileType);
	}

	public static int adapterFileIcon(FileType fileType) {
		switch (fileType) {
		case IMAGE:
			return R.drawable.selectfile_type_picture;
		case WORD:
			return R.drawable.selectfile_type_word;
		case EXCEL:
			return R.drawable.selectfile_type_excel;
		case PDF:
			return R.drawable.selectfile_type_pdf;
		case PPT:
			return R.drawable.selectfile_type_ppt;
		case ZIP:
			return R.drawable.selectfile_type_zip;
		case VIS:
			return R.drawable.selectfile_type_viso;
		case VIDEO:
			return R.drawable.selectfile_type_video;
		case AUDIO:
			return R.drawable.selectfile_type_sound;
		case UNKNOW:
			return R.drawable.selectfile_type_ohter;
		default:
			return R.drawable.selectfile_type_ohter;
		}
	}

	public static FileType getFileType(String postfixName) {
		postfixName = postfixName.toLowerCase();
		if (checkEndsWithInStringArray(postfixName, GlobalConfig.APPLICATION_CONTEXT.getResources().getStringArray(R.array.fileEndingImage))) {
			return FileType.IMAGE;
		} else if (checkEndsWithInStringArray(postfixName,
				GlobalConfig.APPLICATION_CONTEXT.getResources().getStringArray(R.array.fileEndingWebText))) {
			return FileType.HTML;
		} else
			if (checkEndsWithInStringArray(postfixName, GlobalConfig.APPLICATION_CONTEXT.getResources().getStringArray(R.array.fileEndingAPK))) {
			return FileType.PACKAGE;

		} else if (checkEndsWithInStringArray(postfixName,
				GlobalConfig.APPLICATION_CONTEXT.getResources().getStringArray(R.array.fileEndingPackage))) {
			return FileType.ZIP;

		} else if (checkEndsWithInStringArray(postfixName,
				GlobalConfig.APPLICATION_CONTEXT.getResources().getStringArray(R.array.fileEndingAudio))) {
			return FileType.AUDIO;
		} else if (checkEndsWithInStringArray(postfixName,
				GlobalConfig.APPLICATION_CONTEXT.getResources().getStringArray(R.array.fileEndingVideo))) {
			return FileType.VIDEO;
		} else if (checkEndsWithInStringArray(postfixName,
				GlobalConfig.APPLICATION_CONTEXT.getResources().getStringArray(R.array.fileEndingText))) {
			return FileType.TEXT;
		} else
			if (checkEndsWithInStringArray(postfixName, GlobalConfig.APPLICATION_CONTEXT.getResources().getStringArray(R.array.fileEndingPdf))) {
			return FileType.PDF;
		} else if (checkEndsWithInStringArray(postfixName,
				GlobalConfig.APPLICATION_CONTEXT.getResources().getStringArray(R.array.fileEndingWord))) {
			return FileType.WORD;
		} else if (checkEndsWithInStringArray(postfixName,
				GlobalConfig.APPLICATION_CONTEXT.getResources().getStringArray(R.array.fileEndingExcel))) {
			return FileType.EXCEL;
		} else
			if (checkEndsWithInStringArray(postfixName, GlobalConfig.APPLICATION_CONTEXT.getResources().getStringArray(R.array.fileEndingPPT))) {
			return FileType.PPT;
		} else if (checkEndsWithInStringArray(postfixName,
				GlobalConfig.APPLICATION_CONTEXT.getResources().getStringArray(R.array.fileEndingVis))) {
			return FileType.VIS;
		}
		{
			return FileType.UNKNOW;
		}
	}

	public static void openFile(String filePath) {
		if (!TextUtils.isEmpty(filePath))
			openFile(new File(filePath));
		else
			Toast.makeText(GlobalConfig.APPLICATION_CONTEXT, R.string.util_file_toast_error, Toast.LENGTH_SHORT).show();

	}

	public static void openFile(File file) {

		// 4、通过调用OpenFileUitls类返回的Intent，打开相应的文件
		if (file != null && file.exists() && file.isFile()) {
			String filePath = file.getAbsolutePath();
			int dot = filePath.lastIndexOf(".");
			if (dot == -1) {
				Toast.makeText(GlobalConfig.APPLICATION_CONTEXT, R.string.util_file_toast_error, Toast.LENGTH_SHORT).show();
				return;
			}

			String postfixName = filePath.substring(dot).toLowerCase();
			Intent intent;
			try {
				if (checkEndsWithInStringArray(postfixName,
						GlobalConfig.APPLICATION_CONTEXT.getResources().getStringArray(R.array.fileEndingImage))) {
					intent = FileUtils.getImageFileIntent(file);
					GlobalConfig.APPLICATION_CONTEXT.startActivity(intent);
				} else if (checkEndsWithInStringArray(postfixName,
						GlobalConfig.APPLICATION_CONTEXT.getResources().getStringArray(R.array.fileEndingWebText))) {
					intent = FileUtils.getHtmlFileIntent(file);
					GlobalConfig.APPLICATION_CONTEXT.startActivity(intent);
				} else if (checkEndsWithInStringArray(postfixName,
						GlobalConfig.APPLICATION_CONTEXT.getResources().getStringArray(R.array.fileEndingAPK))) {
					intent = FileUtils.getApkFileIntent(file);
					GlobalConfig.APPLICATION_CONTEXT.startActivity(intent);

				} else if (checkEndsWithInStringArray(postfixName,
						GlobalConfig.APPLICATION_CONTEXT.getResources().getStringArray(R.array.fileEndingPackage))) {
					intent = FileUtils.getApplicationFileIntent(file);
					GlobalConfig.APPLICATION_CONTEXT.startActivity(intent);

				} else if (checkEndsWithInStringArray(postfixName,
						GlobalConfig.APPLICATION_CONTEXT.getResources().getStringArray(R.array.fileEndingAudio))) {
					intent = FileUtils.getAudioFileIntent(file);
					GlobalConfig.APPLICATION_CONTEXT.startActivity(intent);
				} else if (checkEndsWithInStringArray(postfixName,
						GlobalConfig.APPLICATION_CONTEXT.getResources().getStringArray(R.array.fileEndingVideo))) {
					intent = FileUtils.getVideoFileIntent(file);
					GlobalConfig.APPLICATION_CONTEXT.startActivity(intent);
				} else if (checkEndsWithInStringArray(postfixName,
						GlobalConfig.APPLICATION_CONTEXT.getResources().getStringArray(R.array.fileEndingText))) {
					intent = FileUtils.getTextFileIntent(file);
					GlobalConfig.APPLICATION_CONTEXT.startActivity(intent);
				} else if (checkEndsWithInStringArray(postfixName,
						GlobalConfig.APPLICATION_CONTEXT.getResources().getStringArray(R.array.fileEndingPdf))) {
					intent = FileUtils.getPdfFileIntent(file);
					GlobalConfig.APPLICATION_CONTEXT.startActivity(intent);
				} else if (checkEndsWithInStringArray(postfixName,
						GlobalConfig.APPLICATION_CONTEXT.getResources().getStringArray(R.array.fileEndingWord))) {
					intent = FileUtils.getWordFileIntent(file);
					GlobalConfig.APPLICATION_CONTEXT.startActivity(intent);
				} else if (checkEndsWithInStringArray(postfixName,
						GlobalConfig.APPLICATION_CONTEXT.getResources().getStringArray(R.array.fileEndingExcel))) {
					intent = FileUtils.getExcelFileIntent(file);
					GlobalConfig.APPLICATION_CONTEXT.startActivity(intent);
				} else if (checkEndsWithInStringArray(postfixName,
						GlobalConfig.APPLICATION_CONTEXT.getResources().getStringArray(R.array.fileEndingPPT))) {
					intent = FileUtils.getPPTFileIntent(file);
					GlobalConfig.APPLICATION_CONTEXT.startActivity(intent);
				} else if (checkEndsWithInStringArray(postfixName,
						GlobalConfig.APPLICATION_CONTEXT.getResources().getStringArray(R.array.fileEndingVis))) {
					intent = FileUtils.getApplicationFileIntent(file);
					GlobalConfig.APPLICATION_CONTEXT.startActivity(intent);
				} else
					Toast.makeText(GlobalConfig.APPLICATION_CONTEXT, R.string.util_file_toast_error, Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Toast.makeText(GlobalConfig.APPLICATION_CONTEXT, R.string.util_file_toast_error, Toast.LENGTH_SHORT).show();
			}
		}
	}

	// 3、定义用于检查要打开的文件的后缀是否在遍历后缀数组中
	private static boolean checkEndsWithInStringArray(String checkItsEnd, String[] fileEndings) {
		for (String aEnd : fileEndings) {
			if (checkItsEnd.endsWith(aEnd))
				return true;
		}
		return false;
	}

	// android获取一个用于打开HTML文件的intent
	public static Intent getHtmlFileIntent(File file) {
		Uri uri = Uri.parse(file.toString()).buildUpon().encodedAuthority("com.android.htmlfileprovider")
				.scheme("content").encodedPath(file.toString()).build();
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.setDataAndType(uri, "text/html");
		return intent;
	}

	// android获取一个用于打开图片文件的intent
	public static Intent getImageFileIntent(File file) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(file);
		intent.setDataAndType(uri, "image/*");
		return intent;
	}

	// android获取一个用于打开PDF文件的intent
	public static Intent getPdfFileIntent(File file) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(file);
		intent.setDataAndType(uri, "application/pdf");
		return intent;
	}

	// android获取一个用于打开文本文件的intent
	public static Intent getTextFileIntent(File file) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(file);
		intent.setDataAndType(uri, "text/plain");
		return intent;
	}

	// android获取一个用于打开音频文件的intent
	public static Intent getAudioFileIntent(File file) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(file);
		intent.setDataAndType(uri, "audio/*");
		return intent;
	}

	// android获取一个用于打开视频文件的intent
	public static Intent getVideoFileIntent(File file) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		// intent.putExtra("oneshot", 0);
		// intent.putExtra("configchange", 0);
		Uri uri = Uri.fromFile(file);
		intent.setDataAndType(uri, "video/*");
		return intent;
	}

	// android获取一个用于打开CHM文件的intent
	public static Intent getChmFileIntent(File file) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(file);
		intent.setDataAndType(uri, "application/x-chm");
		return intent;
	}

	// android获取一个用于打开Word文件的intent
	public static Intent getWordFileIntent(File file) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(file);
		intent.setDataAndType(uri, "application/*");
		return intent;
	}

	// android获取一个用于打开Excel文件的intent
	public static Intent getExcelFileIntent(File file) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(file);
		intent.setDataAndType(uri, "application/vnd.ms-excel");
		return intent;
	}

	// android获取一个用于打开PPT文件的intent
	public static Intent getPPTFileIntent(File file) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(file);
		intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
		return intent;
	}

	// android获取一个用于打开apk文件的intent
	public static Intent getApkFileIntent(File file) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		return intent;
	}

	// android获取一个用于打开apk文件的intent
	public static Intent getApplicationFileIntent(File file) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file), "application/*");
		return intent;
	}

	/**
	 * 获取文件字符串内容
	 * 
	 * @param filePath
	 * @return
	 */
	public static String getFileConent(String filePath) {
		if (TextUtils.isEmpty(filePath))
			return null;

		File srcFile = new File(filePath);
		if (!srcFile.exists()) {
			try {
				srcFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		if (!srcFile.isFile()) {
			return null;
		}

		BufferedReader reader = null;
		StringBuilder sb = new StringBuilder();
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(srcFile)));
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}

	/**
	 * 存储字符串到文件
	 * 
	 * @param filePath
	 * @param content
	 * @return
	 */
	public static boolean save2File(String filePath, String content) {
		if (TextUtils.isEmpty(filePath))
			return false;

		File srcFile = new File(filePath);
		if (!srcFile.exists()) {
			try {
				srcFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		FileWriter write = null;
		try {
			write = new FileWriter(filePath, true);
			write.write(content);
			write.flush();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (write != null) {
				try {
					write.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static Bitmap decodeBitmap(byte[] data) {
		Bitmap bm = BitmapFactory.decodeStream(new ByteArrayInputStream(data));
		return bm;
	}

	public static byte[] getFileBytesFrom(String filePath) {
		return getFileBytesFrom(new File(filePath));
	}

	public static byte[] getFileBytesFrom(File file) {
		byte[] data = null;
		if (file == null) {
			return data;
		}

		FileInputStream stream = null;
		ByteArrayOutputStream out = null;
		try {
			int len = (int) file.length();
			stream = new FileInputStream(file);
			out = new ByteArrayOutputStream(len);
			byte[] b = new byte[1024];
			int n;
			while ((n = stream.read(b)) != -1) {
				out.write(b, 0, n);
				out.flush();
			}
			data = out.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return data;
	}

	/**
	 * Try to return the absolute file path from the given Uri
	 *
	 * @param GlobalConfig.APPLICATION_CONTEXT
	 * @param uri
	 * @return the file path or null
	 */
	public static String getUri2FilePath(final Context context, final Uri uri) {
		if (null == uri)
			return null;
		final String scheme = uri.getScheme();
		String data = null;
		if (scheme == null)
			data = uri.getPath();
		else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
			data = uri.getPath();
		} else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
			Cursor cursor = GlobalConfig.APPLICATION_CONTEXT.getContentResolver().query(uri, new String[] { ImageColumns.DATA }, null, null,
					null);
			if (null != cursor) {
				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(ImageColumns.DATA);
					if (index > -1) {
						data = cursor.getString(index);
					}
				}
				cursor.close();
			}
		}
		return data;
	}


	/**
	 * 删除指定文件
	 *
	 * @param fileNames
	 */
	public static void deleteFiles(String... fileNames) {
		if (fileNames.length <= 0)
			return;
		for (int i = 0; i < fileNames.length; i++) {
			File file = new File(fileNames[i]);
			if (file.exists())
				file.delete();
		}
	}
}
