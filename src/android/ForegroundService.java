package com.mobishift.plugins.foregroundnotification;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Gamma on 15/8/5.
 */
public class ForegroundService extends Service {
    private static final String TAG = "ForegroundService";
    private static final String TYPE_MESSAGE = "message";
    private static final String TYPE_TIME = "time";
    private static final int ID = 10038;

    private String type;
    private Date showTime;
    private String message;
    private Timer timer;
    private Timer notificationTimer;
    private NotificationCompat.Builder notificationBuilder;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (notificationTimer != null) {
            notificationTimer.cancel();
            notificationTimer = null;
        }

        stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        type = intent.getStringExtra("type");
        if(intent.hasExtra("showTime")){
            String showTimeString = intent.getStringExtra("showTime");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
            try{
                showTime = dateFormat.parse(showTimeString);
            }catch (ParseException ex){
                Log.e(TAG, ex.getMessage());
            }
        }else{
            Calendar calendar = Calendar.getInstance();
            showTime = calendar.getTime();
        }

        if(intent.hasExtra("message")){
            message = intent.getStringExtra("message");
        }

        createNotification(intent);

        return START_REDELIVER_INTENT;
    }

    public void showNotification(){
        if(type.equals(TYPE_MESSAGE)){
            notificationBuilder.setContentText(message);
            notificationBuilder.setTicker(message);
            startForeground(ID, notificationBuilder.build());
        }else if(type.equals(TYPE_TIME)){
            String notificationMessage = message.replace("{{datetime}}", "00:00:00");
            notificationBuilder.setContentText(notificationMessage);
            notificationBuilder.setTicker(notificationMessage);
            startForeground(ID, notificationBuilder.build());
            IntervalHandler intervalHandler = new IntervalHandler(this);
//            timeHandler = new Handler(){
//                @Override
//                public void handleMessage(Message msg) {
//                    if(msg.what == 1){
//                        long duration = msg.getData().getLong("duration");
//                        int hour = (int)(duration / 1000.0 / 3600.0);
//                        duration = duration - hour * 3600 * 1000;
//                        int minute = (int)(duration / 1000.0 / 60.0 );
//                        duration = duration - minute * 1000 * 60;
//                        int second = (int)(duration / 1000.0);
//                        StringBuilder stringBuilder = new StringBuilder();
//                        stringBuilder.append(getTime(hour)).append(":").append(getTime(minute)).append(":").append(getTime(second));
//                        String dateTime = stringBuilder.toString();
//                        String notificationMessage = message.replace("{{datetime}}", dateTime);
//                        notificationBuilder.setContentText(notificationMessage);
////                        notificationBuilder.setTicker(notificationMessage);
////                        startForeground(ID, notificationBuilder.build());
////                        notification.contentView.setTextViewText(android.R.id.text1, notificationMessage);
////                        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
////                        manager.notify(ID, notificationBuilder.build());
//                        startForeground(ID, notificationBuilder.build());
//                    }
//                }
//            };

            IntervalTask intervalTask = new IntervalTask(showTime, intervalHandler);
            timer = new Timer();
            timer.schedule(intervalTask, 0, 1000);
        }
    }

    private void createNotification(Intent intent){
        String packageName = this.getPackageName();
        Intent launchIntent = this.getPackageManager().getLaunchIntentForPackage(packageName);
        if(intent.hasExtra("url")){
            launchIntent.putExtra("foregroundUrl", intent.getStringExtra("url"));
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setSmallIcon(this.getApplicationInfo().icon)
                .setContentTitle(this.getString(this.getApplicationInfo().labelRes));

        notificationBuilder.setDefaults(0);
        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.setAutoCancel(false);

        Calendar calendar =  Calendar.getInstance();
        if(showTime == null){
            showTime = calendar.getTime();
        }

        long time = showTime.getTime() - calendar.getTimeInMillis();
        if(time < 0){
            time = 0;
        }

        final ShowHandler showHandler = new ShowHandler(this);
        notificationTimer = new Timer();
        notificationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                showHandler.sendEmptyMessage(1);
                notificationTimer = null;
            }
        }, time);
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                showNotification();
//            }
//        }, time);
    }

    private class IntervalTask extends TimerTask{
        private Handler handler;
        private Date startTime;
        private long duration;

        public IntervalTask(Date startTime, Handler handler){
            this.startTime = startTime;
            this.handler = handler;
            duration = 0;
        }

        @Override
        public void run() {
            Calendar now = Calendar.getInstance();
            duration = now.getTimeInMillis() - this.startTime.getTime();

            Message message = Message.obtain();
            message.what = 1;
            Bundle bundle = new Bundle();
            bundle.putLong("duration", duration);
            message.setData(bundle);
            handler.sendMessage(message);
        }

        public long getDuration() {
            return duration;
        }
    }

    private static class IntervalHandler extends Handler{
        private ForegroundService foregroundService;

        public IntervalHandler(ForegroundService foregroundService){
            this.foregroundService = foregroundService;
        }

        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 1){
                long duration = msg.getData().getLong("duration");
                int hour = (int)(duration / 1000.0 / 3600.0);
                duration = duration - hour * 3600 * 1000;
                int minute = (int)(duration / 1000.0 / 60.0 );
                duration = duration - minute * 1000 * 60;
                int second = (int)(duration / 1000.0);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(getTime(hour)).append(":").append(getTime(minute)).append(":").append(getTime(second));
                String dateTime = stringBuilder.toString();
                String notificationMessage = foregroundService.message.replace("{{datetime}}", dateTime);
                foregroundService.notificationBuilder.setContentText(notificationMessage);
//                        notificationBuilder.setTicker(notificationMessage);
//                        startForeground(ID, notificationBuilder.build());
//                        notification.contentView.setTextViewText(android.R.id.text1, notificationMessage);
//                        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//                        manager.notify(ID, notificationBuilder.build());
                foregroundService.startForeground(ID, foregroundService.notificationBuilder.build());
            }
        }

        private String getTime(int time){
            if(time < 10){
                return "0" +  time;
            }
            return String.valueOf(time);
        }
    }

    private  static class ShowHandler extends Handler{
        private ForegroundService foregroundService;

        public ShowHandler(ForegroundService foregroundService){
            this.foregroundService = foregroundService;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                this.foregroundService.showNotification();
            }
        }
    }
}
