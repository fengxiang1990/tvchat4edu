package com.bizcom.util;

import android.app.Dialog;
import android.content.Context;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.bizcom.vc.widget.cus.CustomDialog;
import com.shdx.tvchat.phone.R;

/**
 * For example :
 * <p>
 * <pre>
 * Resources res = getResources();
 * mQuitDialog = DialogManager.getInstance().showQuitModeDialog(
 * 	DialogManager.getInstance().new DialogInterface(
 * 		mContext,res.getString(R.string.xxx) ,
 * 			res.getString(R.string.xxx) ,
 * 			res.getString(R.string.xxx) ,
 * 			res.getString(R.string.xxx)) {
 * 		public void confirmCallBack() {}
 *
 * 		public void cannelCallBack() {};
 * mQuitDialog.show();
 *
 * 如果使用该类，需要再Activity销毁时，调用clearDialogObject函数，用来解除绑定关系。
 * </pre>
 *
 * @author
 */
public class DialogManager {

    private static DialogManager dialogManager;
    private Dialog normalDialog;

    public static synchronized DialogManager getInstance() {
        if (dialogManager == null) {
            dialogManager = new DialogManager();
        }
        return dialogManager;
    }

    private TextView dialogTitle;
    private TextView dialogContent;
    private TextView confirmButtonContent;
    private TextView cannelButtonContent;

    /**
     * 显示带有标题，内容，确定和取消的标准对话框。
     *
     * @param inter
     * @return
     */
    public Dialog showNormalModeDialog(final DialogInterface inter) {
        normalDialog = null;
        if (normalDialog == null) {
            normalDialog = new CustomDialog(inter.mContext,
                    R.style.InMeetingQuitDialog);
            android.view.WindowManager.LayoutParams lp = normalDialog.getWindow().getAttributes();
            lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            normalDialog.setContentView(R.layout.dialog_normal);
            dialogTitle = (TextView) normalDialog
                    .findViewById(R.id.dialog_title_content);
            dialogContent = (TextView) normalDialog
                    .findViewById(R.id.ws_normalDialog_content);
            dialogContent.setGravity(Gravity.CENTER);
            confirmButtonContent = (TextView) normalDialog
                    .findViewById(R.id.ws_normalDialog_confirm);
            cannelButtonContent = (TextView) normalDialog
                    .findViewById(R.id.ws_normalDialog_cannel);
        }

        dialogTitle.setText(inter.title);
        dialogContent.setText(inter.content);
        confirmButtonContent.setText(inter.quitButtonContent);
        cannelButtonContent.setText(inter.cannelButtonContent);

        cannelButtonContent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                inter.cannelCallBack();
            }

        });

        confirmButtonContent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                inter.confirmCallBack();
            }

        });
        return normalDialog;
    }

    public Dialog showSingleNoTitleDialog(DialogInterface inter) {
        // View inflate = View.inflate(inter.mContext, R.layout.dialog_no_title,
        // null);
        // LayoutParams layoutParams = inflate.getLayoutParams();
        // layoutParams.width = DensityUtils.dip2px(inter.mContext, 270);
        // layoutParams.height = DensityUtils.dip2px(inter.mContext, 170);
        // inflate.setLayoutParams(layoutParams);
        CustomDialog showNoTitleDialog = (CustomDialog) showNoTitleDialog(inter);
        cannelButtonContent.setVisibility(View.GONE);
        showNoTitleDialog.cusWidth = DensityUtils.dip2px(inter.mContext, 270);
        showNoTitleDialog.cusHeight = DensityUtils.dip2px(inter.mContext, 170);
        return showNoTitleDialog;
    }

    /**
     * 显示带有内容，确定和取消的标准对话框，没有标题。
     *
     * @param inter
     * @return
     */
    public Dialog showNoTitleDialog(final DialogInterface inter) {
        normalDialog = null;
        if (normalDialog == null) {
            normalDialog = new CustomDialog(inter.mContext, R.style.customDialog);
            normalDialog.setContentView(R.layout.dialog_no_title);
            dialogContent = (TextView) normalDialog
                    .findViewById(R.id.title);
            dialogContent.setGravity(Gravity.CENTER);
            confirmButtonContent = (TextView) normalDialog
                    .findViewById(R.id.ok);
            cannelButtonContent = (TextView) normalDialog
                    .findViewById(R.id.cancel);
        }

        dialogContent.setText(Html.fromHtml(String.valueOf(inter.content)));
        confirmButtonContent.setText(Html.fromHtml(String.valueOf(inter.quitButtonContent)));
        if (inter.cannelButtonContent != null)
            cannelButtonContent.setText(inter.cannelButtonContent);

        cannelButtonContent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                inter.cannelCallBack();
            }
        });

        confirmButtonContent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                inter.confirmCallBack();
            }

        });
        return normalDialog;
    }

    public Dialog showNoTitleDialog2(final DialogInterface inter) {
        normalDialog = null;
        if (normalDialog == null) {
            normalDialog = new CustomDialog(inter.mContext, R.style.customDialog);
            normalDialog.setContentView(R.layout.dialog_no_title2);
            normalDialog.setCancelable(false);
            dialogContent = (TextView) normalDialog
                    .findViewById(R.id.title);
            dialogContent.setGravity(Gravity.CENTER);
            cannelButtonContent = (TextView) normalDialog
                    .findViewById(R.id.cancel);
        }

        dialogContent.setText(Html.fromHtml(String.valueOf(inter.content)));
        if (inter.cannelButtonContent != null)
            cannelButtonContent.setText(inter.cannelButtonContent);

        cannelButtonContent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                inter.cannelCallBack();
            }
        });

        return normalDialog;
    }

    /**
     * 可以编辑的dialog
     *
     * @param inter
     * @return
     */
    public Dialog showEditDialog(final DialogInterface inter) {
        normalDialog = null;
        if (normalDialog == null) {
            normalDialog = new CustomDialog(inter.mContext, R.style.customDialog);
            normalDialog.setContentView(R.layout.et_dialog);

            confirmButtonContent = (TextView) normalDialog
                    .findViewById(R.id.ok);
            cannelButtonContent = (TextView) normalDialog
                    .findViewById(R.id.cancel);
        }

        confirmButtonContent.setText(Html.fromHtml(String.valueOf(inter.quitButtonContent)));
        if (inter.cannelButtonContent != null)
            cannelButtonContent.setText(inter.cannelButtonContent);

        cannelButtonContent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                inter.cannelCallBack();
            }
        });

        confirmButtonContent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                inter.confirmCallBack();
            }

        });
        return normalDialog;
    }

    public void setDialogContent(CharSequence content) {
        dialogContent.setText(content);
    }

    public void changeDialogGlobal() {
        normalDialog.getWindow().setType(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    }

    public void clearDialogObject() {
        if (normalDialog != null)
            normalDialog = null;
    }

    public abstract class DialogInterface {

        private Context mContext;
        private CharSequence title;
        private CharSequence content;
        private CharSequence quitButtonContent;
        private CharSequence cannelButtonContent;

        public DialogInterface(Context mContext, CharSequence title,
                               CharSequence content, CharSequence quitButtonContent,
                               CharSequence cannelButtonContent) {
            this.mContext = mContext;
            this.title = title;
            this.content = content;
            this.quitButtonContent = quitButtonContent;
            this.cannelButtonContent = cannelButtonContent;
        }

        public abstract void confirmCallBack();

        public abstract void cannelCallBack();
    }
}
