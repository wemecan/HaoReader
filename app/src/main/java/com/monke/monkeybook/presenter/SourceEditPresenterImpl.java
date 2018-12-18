package com.monke.monkeybook.presenter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.monke.basemvplib.BasePresenterImpl;
import com.monke.basemvplib.impl.IView;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.dao.DbHelper;
import com.monke.monkeybook.help.Constant;
import com.monke.monkeybook.help.FormatWebText;
import com.monke.monkeybook.model.BookSourceManager;
import com.monke.monkeybook.presenter.contract.SourceEditContract;
import com.monke.monkeybook.utils.BitmapUtil;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by GKF on 2018/1/28.
 * 编辑书源
 */

public class SourceEditPresenterImpl extends BasePresenterImpl<SourceEditContract.View> implements SourceEditContract.Presenter {

    @Override
    public void saveSource(BookSourceBean bookSource, BookSourceBean bookSourceOld) {
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            if (bookSourceOld != null && !Objects.equals(bookSource.getBookSourceUrl(), bookSourceOld.getBookSourceUrl())) {
                DbHelper.getInstance().getmDaoSession().getBookSourceBeanDao().delete(bookSourceOld);
            }
            BookSourceManager.getInstance().addBookSource(bookSource);
            BookSourceManager.getInstance().refreshBookSource();
            e.onNext(true);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        mView.saveSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void copySource(BookSourceBean bookSourceBean) {
        ClipboardManager clipboard = (ClipboardManager) mView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(null, mView.getBookSourceStr());
        if (clipboard != null) {
            clipboard.setPrimaryClip(clipData);
            mView.showSnackBar("拷贝成功");
        }
    }

    @Override
    public void pasteSource() {
        ClipboardManager clipboard = (ClipboardManager) mView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = clipboard != null ? clipboard.getPrimaryClip() : null;
        if (clipData != null && clipData.getItemCount() > 0) {
            setText(String.valueOf(clipData.getItemAt(0).getText()));
        }
    }

    @Override
    public void setText(String bookSourceStr) {
        Gson gson = new Gson();
        try {
            BookSourceBean bookSourceBean = gson.fromJson(bookSourceStr, BookSourceBean.class);

            if(!Arrays.asList(Constant.BOOK_TYPES).contains(bookSourceBean.getBookSourceType())){
                bookSourceBean.setBookSourceType(Constant.BookType.TEXT);
            }

            if(!Arrays.asList(Constant.RULE_TYPES).contains(bookSourceBean.getBookSourceRuleType())){
                bookSourceBean.setBookSourceRuleType(Constant.RuleType.DEFAULT);
            }

            mView.setText(bookSourceBean);
        }catch (Exception ignore){
            mView.showSnackBar("数据格式不对");
        }
    }

    @Override
    public Bitmap encodeAsBitmap(String str) {
        Bitmap bitmap = null;
        BitMatrix result;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            Hashtable<EncodeHintType, Object> hst = new Hashtable<>();
            hst.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hst.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            result = multiFormatWriter.encode(str, BarcodeFormat.QR_CODE, 600, 600, hst);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(result);
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException iae) { // ?
            return null;
        }
        return bitmap;
    }

    @Override
    public void analyzeBitmap(String path) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            bitmap = BitmapUtil.getImage(bitmap);

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

            RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
            Reader reader = new MultiFormatReader();
            Result result;

            try {
                result = reader.decode(binaryBitmap);
                setText(result.getText());
            } catch (NotFoundException | ChecksumException | FormatException e) {
                e.printStackTrace();
                mView.showSnackBar("解析图片错误");
            }

        } catch (Exception e) {
            e.printStackTrace();
            mView.showSnackBar("图片获取错误");
        }

    }

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
    }

    @Override
    public void detachView() {

    }
}
