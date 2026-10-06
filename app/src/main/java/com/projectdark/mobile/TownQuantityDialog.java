package com.projectdark.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.text.InputFilter;
import android.text.InputType;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import java.util.function.IntConsumer;

/** Native number entry for touch shops; cancel never changes the transaction. */
final class TownQuantityDialog {
  static void show(Context context,String name,int current,IntConsumer accepted){
    Context host=context;
    while(!(host instanceof Activity)&&host instanceof ContextWrapper){
      Context base=((ContextWrapper)host).getBaseContext();if(base==host)break;host=base;
    }
    if(!(host instanceof Activity)||((Activity)host).isFinishing())return;
    final Context activity=host;
    EditText input=new EditText(activity);
    input.setInputType(InputType.TYPE_CLASS_NUMBER);
    input.setSingleLine(true);input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
    input.setText(String.valueOf(current));input.setSelectAllOnFocus(true);
    input.setHint("1~999999");input.setContentDescription(name+" 수량");
    input.setPadding(28,16,28,16);
    AlertDialog dialog=new AlertDialog.Builder(activity).setTitle(name+" · 수량")
      .setView(input).setNegativeButton("취소",null).setPositiveButton("적용",null).create();
    dialog.setOnShowListener(d->{
      dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
        int value=parse(input.getText().toString());
        if(value<1){input.setError("1~999999 사이의 수량을 입력해 주세요.");return;}
        accepted.accept(value);dialog.dismiss();
      });
      input.requestFocus();input.selectAll();
      dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
      input.post(()->((InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(input,InputMethodManager.SHOW_IMPLICIT));
    });
    dialog.setOnDismissListener(d->((InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(input.getWindowToken(),0));
    dialog.show();
  }
  static int parse(String text){try{int n=Integer.parseInt(text.trim());return n>=1&&n<=999999?n:-1;}catch(RuntimeException e){return -1;}}
}
