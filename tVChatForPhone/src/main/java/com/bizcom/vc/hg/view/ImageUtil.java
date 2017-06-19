package com.bizcom.vc.hg.view;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class ImageUtil {
	/** 
	 * ͨ��ExifInterface���ȡͼƬ�ļ��ı���ת�Ƕ� 
	 * @param path �� ͼƬ�ļ���·�� 
	 * @return ͼƬ�ļ��ı���ת�Ƕ� 
	 */  
	@SuppressLint("NewApi")
	public static int readPicDegree(String path) {  
		int degree = 0;  

		// ��ȡͼƬ�ļ���Ϣ����ExifInterface  
		ExifInterface exif = null;  
		try {  
			exif = new ExifInterface(path);  
		} catch (Exception e) {  
			// TODO Auto-generated catch block  
			e.printStackTrace();  
		}  

		if (exif != null) {  
			int orientation = exif.getAttributeInt(  
					ExifInterface.TAG_ORIENTATION,  
					ExifInterface.ORIENTATION_NORMAL);  
			switch (orientation) {  
			case ExifInterface.ORIENTATION_ROTATE_90:  
				degree = 90;  
				break;  

			case ExifInterface.ORIENTATION_ROTATE_180:  
				degree = 180;  
				break;  

			case ExifInterface.ORIENTATION_ROTATE_270:  
				degree = 270;  
				break;  
			}  
		}  

		return degree;  
	}  

	public static Bitmap rotateBitmap(int degree, Bitmap bitmap) {  
		Matrix matrix = new Matrix();  
		matrix.postRotate(degree);  

		Bitmap bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),  
				bitmap.getHeight(), matrix, true);  
		return bm;  
	}  


	/**
	 * 加载本地图片
	 * @return
	 */
	public static Bitmap getLoacalBitmap(String filePath,int w,int h) {
//		File  f=new File(filePath);
//
//		Bitmap bitmap = null;
//		BitmapFactory.Options opts = new BitmapFactory.Options();
//		opts.inJustDecodeBounds = true;
//		BitmapFactory.decodeFile(filePath, opts);
//
//		if(f.length()/1024>800){
//			opts.inSampleSize =2;
//		}else if(f.length()/1024>(1024*2)){
//			opts.inSampleSize =4;
//		}else if(f.length()/1024<=1024){
//			opts.inSampleSize =1;
//		}else{
//			opts.inSampleSize =6;
//		}
//		opts.inJustDecodeBounds = false;
//
//		try {
//			bitmap = BitmapFactory.decodeFile(filePath, opts);
//
//		}catch (Exception e) {
//			// TODO: handle exception
//		}
//		return bitmap;

		Bitmap bitmap = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, opts);

		opts.inSampleSize = computeSampleSize(opts, -1, (w*h*2));
		opts.inJustDecodeBounds = false;

		try {
			bitmap = BitmapFactory.decodeFile(filePath, opts);

			compressImage(bitmap);
		}catch (Exception e) {
			// TODO: handle exception
		}
		return bitmap;
}

	public static Bitmap compressImage(Bitmap image) { 
		if (image==null) return null; 

		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中  
		int options = 100;  
		while ( baos.toByteArray().length / 1024>400) {  //循环判断如果压缩后图片是否大于400kb,大于继续压缩
			baos.reset();//重置baos即清空baos  
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中  
			options -= 5;//每次都减少10  
		}  
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中  
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片  
		return bitmap;  
	}  

	/**
	 * 加载本地图片
	 * @return
	 */
	public static Bitmap getLoacalBitmap(byte[] data) {
		try {
			return BitmapFactory.decodeByteArray(data, 0, data.length-1) ;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public static Bitmap createImageThumbnail(String filePath){  
		Bitmap bitmap = null;  

		try {  
			bitmap = BitmapFactory.decodeFile(filePath);  
		}catch (Exception e) {  
			// TODO: handle exception  
		}  
		return bitmap;  
	}  

	public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {  
		int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);  
		int roundedSize;  
		if (initialSize <= 8) {  
			roundedSize = 1;  
			while (roundedSize < initialSize) {  
				roundedSize <<= 1;  
			}  
		} else {  
			roundedSize = (initialSize + 7) / 8 * 8;  
		}  
		return roundedSize;  
	}  

	private static int computeInitialSampleSize(BitmapFactory.Options options,int minSideLength, int maxNumOfPixels) {  
		double w = options.outWidth;  
		double h = options.outHeight;  
		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));  
		int upperBound = (minSideLength == -1) ? 128 :(int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));  
		if (upperBound < lowerBound) {  
			// return the larger one when there is no overlapping zone.  
			return lowerBound;  
		}  
		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {  
			return 1;  
		} else if (minSideLength == -1) {  
			return lowerBound;  
		} else {  
			return upperBound;  
		}  
	}  


	/** 保存方法 */
	public static Map<String,String>  saveBitmap(Bitmap bm) {
		Map<String , String > map=null;
		try {
			
		String path="";
		String name="pic.jpg";
		if((Environment.getExternalStorageState()).equals(Environment.MEDIA_MOUNTED)){
		path = Environment.getExternalStorageDirectory().getAbsolutePath();
		}

		File f = new File(path, name);
		if (f.exists()) 
			f.delete();
		
		f.createNewFile();
		FileOutputStream out = new FileOutputStream(f);
		bm.compress(Bitmap.CompressFormat.PNG, 90, out);
		out.flush();
		out.close();
		
		map=new HashMap<String, String>();
		map.put("fileName", name);
		map.put("filePath", path);
		
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return map;

	}



	@SuppressLint("NewApi")
	public static String getPath(final Context context, final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}

			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {
				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] {
						split[1]
				};

				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			// Return the remote address
			if (isGooglePhotosUri(uri))
				return uri.getLastPathSegment();

			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	public static String getDataColumn(Context context, Uri uri, String selection,
									   String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {
				column
		};

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
					null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}


	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}


	/**
	 * 根据Uri获取图片绝对路径，解决Android4.4以上版本Uri转换
	 *
	 * @param context
	 * @param imageUri
	 */
	public static String getImageAbsolutePath(Context context, Uri imageUri) {
		if (context == null || imageUri == null)
			return null;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, imageUri)) {
			if (isExternalStorageDocument(imageUri)) {
				String docId = DocumentsContract.getDocumentId(imageUri);
				String[] split = docId.split(":");
				String type = split[0];
				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}
			} else if (isDownloadsDocument(imageUri)) {
				String id = DocumentsContract.getDocumentId(imageUri);
				Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
				return getDataColumn(context, contentUri, null, null);
			} else if (isMediaDocument(imageUri)) {
				String docId = DocumentsContract.getDocumentId(imageUri);
				String[] split = docId.split(":");
				String type = split[0];
				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}
				String selection = MediaStore.Images.Media._ID + "=?";
				String[] selectionArgs = new String[]{split[1]};
				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		} // MediaStore (and general)
		else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
			// Return the remote address
			if (isGooglePhotosUri(imageUri))
				return imageUri.getLastPathSegment();
			return getDataColumn(context, imageUri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
			return imageUri.getPath();
		}
		return null;
	}






}
