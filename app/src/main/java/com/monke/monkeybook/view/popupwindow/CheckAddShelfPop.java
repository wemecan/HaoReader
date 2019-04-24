//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook.view.popupwindow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.monke.monkeybook.R;
import com.monke.monkeybook.utils.ScreenUtils;

public class CheckAddShelfPop extends PopupWindow {
    private Context mContext;
    private View view;
    private boolean isAudioBook;

    public interface OnItemClickListener {
        void clickExit();

        void clickAddShelf();
    }

    private OnItemClickListener itemClick;
    private String bookName;

    public CheckAddShelfPop(Context context, @NonNull String bookName, @NonNull OnItemClickListener itemClick, boolean isAudioBook) {
        super(ScreenUtils.dpToPx(300f), ViewGroup.LayoutParams.WRAP_CONTENT);
        mContext = context;
        this.isAudioBook = isAudioBook;
        this.bookName = bookName;
        this.itemClick = itemClick;
        view = LayoutInflater.from(mContext).inflate(R.layout.moprogress_dialog_two, null);
        this.setContentView(view);

        initView();
        setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.shape_pop_checkaddshelf_bg));
        setFocusable(true);
        setTouchable(true);
        setAnimationStyle(R.style.anim_pop_checkaddshelf);
    }

    private void initView() {
        TextView tvBookName = view.findViewById(R.id.tv_msg);
        tvBookName.setText(mContext.getString(R.string.check_add_bookshelf, bookName));
        TextView tvExit = view.findViewById(R.id.tv_cancel);
        tvExit.setText(isAudioBook ? "退出听书" : "退出阅读");
        tvExit.setOnClickListener(v -> {
            dismiss();
            itemClick.clickExit();
        });
        TextView tvAddShelf = view.findViewById(R.id.tv_done);
        tvAddShelf.setText("放入书架");
        tvAddShelf.setOnClickListener(v -> itemClick.clickAddShelf());
    }
}
