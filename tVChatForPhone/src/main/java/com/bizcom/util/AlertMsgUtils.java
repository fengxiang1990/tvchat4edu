package com.bizcom.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bizcom.vc.hg.util.UserHeaderImgHelper;
import com.bizcom.vo.User;
import com.config.GlobalHolder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.shdx.tvchat.phone.R;


/**
 * Created by fengxiang on 2016/11/16.
 */
public class AlertMsgUtils {

    public interface OnDialogBtnClickListener {
        void onConfirm(Dialog dialog);
    }

    public interface OnDialogStringBtnClickListener {
        void onConfirm(String commentName);
    }


    public interface OnDialogBtnsClickListener {
        void onBtn1Click(Dialog dialog);

        void onBtn2Click(Dialog dialog);
    }

    public static void showConfirm(final Context context, String textYes, String textNo, String msg, final OnDialogBtnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_message, null);
        TextView text_msg = (TextView) view.findViewById(R.id.text_msg);
        TextView text_yes = (TextView) view.findViewById(R.id.text_yes);
        TextView text_no = (TextView) view.findViewById(R.id.text_no);
        text_no.setText(textNo);
        text_yes.setText(textYes);
        text_msg.setText(msg);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        text_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        text_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onConfirm(dialog);
            }
        });
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
    }

    static Handler handler = new Handler();

    public static void show(Context context, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_message, null);
        TextView text_msg = (TextView) view.findViewById(R.id.text_msg);
        TextView text_yes = (TextView) view.findViewById(R.id.text_yes);
        TextView text_no = (TextView) view.findViewById(R.id.text_no);
        View v_line = view.findViewById(R.id.v_line);
        v_line.setVisibility(View.GONE);
        text_no.setVisibility(View.GONE);
        text_yes.setText("确定");
        text_msg.setText(msg);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        text_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        }, 1500);
    }


    public static void show(Context context, String msg, final OnDialogBtnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_message, null);
        TextView text_msg = (TextView) view.findViewById(R.id.text_msg);
        TextView text_yes = (TextView) view.findViewById(R.id.text_yes);
        TextView text_no = (TextView) view.findViewById(R.id.text_no);
        View v_line = view.findViewById(R.id.v_line);
        v_line.setVisibility(View.GONE);
        text_no.setVisibility(View.GONE);
        text_yes.setText("确定");
        text_msg.setText(msg);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        text_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onConfirm(dialog);
            }
        });
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
    }


    /**
     * 生成聊口令对话框
     *
     * @param context
     * @param btn1
     * @param btn2
     * @param pwdstr
     * @param listener
     */
    public static void showChatPwdDialog(final Context context, String btn1, String btn2, final String pwdstr, final OnDialogBtnsClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_message_chat_pwd, null);
        TextView text_btn1 = (TextView) view.findViewById(R.id.text_btn1);
        TextView text_btn2 = (TextView) view.findViewById(R.id.text_btn2);
        ImageView btn_close = (ImageView) view.findViewById(R.id.btn_close);
        TextView text_msg = (TextView) view.findViewById(R.id.text_msg);

        text_msg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                String tvl_chat_pwd_hint = context.getResources().getString(R.string.tvl_chat_pwd_hint);
                if (cmb != null) {
                    if (Build.VERSION.SDK_INT >= 21) {
                        ClipData clip = cmb.getPrimaryClip();
                        if (clip != null) {
                            cmb.setPrimaryClip(ClipData.newPlainText("tvl_chat_pwd", tvl_chat_pwd_hint + pwdstr));
                        }else{
                            cmb.setText(tvl_chat_pwd_hint + pwdstr);
                        }
                    } else {
                        cmb.setText(tvl_chat_pwd_hint + pwdstr);
                    }
                }

                Toast.makeText(context, "口令已复制", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        text_btn1.setText(btn1);
        text_btn2.setText(btn2);
        text_msg.setText(text_msg.getText().toString() + pwdstr);


        builder.setView(view);
        final AlertDialog dialog = builder.create();
        text_btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onBtn1Click(dialog);
            }
        });
        text_btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onBtn2Click(dialog);
            }
        });

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
    }


    public static void showAddFrendByChatPwdConfirm(final Context context, User user, final OnDialogBtnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_frend_by_chart_pwd, null);
        TextView text_yes = (TextView) view.findViewById(R.id.text_yes);
        TextView text_no = (TextView) view.findViewById(R.id.text_no);
        TextView text_name = (TextView) view.findViewById(R.id.text_name);
        TextView text_account = (TextView) view.findViewById(R.id.text_account);
        SimpleDraweeView img_header = (SimpleDraweeView) view.findViewById(R.id.img_header);
        text_name.setText(user.getDisplayName());
        text_account.setText(user.getAccount());
        UserHeaderImgHelper.display(img_header, user);
        builder.setView(view);
        boolean isfriend = GlobalHolder.getInstance().isFriend(user);
        if (isfriend) {
            text_yes.setText("查看好友详情");
        }
        final AlertDialog dialog = builder.create();
        text_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        text_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onConfirm(dialog);
            }
        });
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
    }

    public static void showEditCommentNameConfirm(final Context context, String name, final OnDialogStringBtnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_comment_name, null);
        TextView text_yes = (TextView) view.findViewById(R.id.text_yes);
        TextView text_no = (TextView) view.findViewById(R.id.text_no);
        final EditText text_commentName = (EditText) view.findViewById(R.id.input_comment);
        text_commentName.setText(name);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        text_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageUtil.hideKeyBoard(context, text_commentName.getWindowToken());
                dialog.dismiss();
            }
        });
        text_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageUtil.hideKeyBoard(context, text_commentName.getWindowToken());
                String commmentName = text_commentName.getText().toString().trim();
                listener.onConfirm(commmentName);
                dialog.dismiss();
            }
        });
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
    }


    public static void showEditTVCommentNameConfirm(final Context context, String title, final OnDialogStringBtnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_update_tv_remark, null);
        TextView text_yes = (TextView) view.findViewById(R.id.text_yes);
        TextView text_no = (TextView) view.findViewById(R.id.text_no);
        TextView text_title = (TextView) view.findViewById(R.id.text_title);
        final EditText text_commentName = (EditText) view.findViewById(R.id.input_comment);
        // text_commentName.setText(name);
        //text_title.set
        text_title.setText(title);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        text_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onConfirm(null);
                MessageUtil.hideKeyBoard(context, text_commentName.getWindowToken());
                dialog.dismiss();
            }
        });
        text_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageUtil.hideKeyBoard(context, text_commentName.getWindowToken());
                String commmentName = text_commentName.getText().toString().trim();
                listener.onConfirm(commmentName);
                dialog.dismiss();
            }
        });
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
    }

}
