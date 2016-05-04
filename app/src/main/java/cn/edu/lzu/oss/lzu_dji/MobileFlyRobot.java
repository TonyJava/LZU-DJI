package cn.edu.lzu.oss.lzu_dji;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dji.sdk.FlightController.*;

/**
 * Created by hopef on 2016/4/30.
 */
public class MobileFlyRobot {

    private static String TAG = MobileFlyRobot.class.getName();

    //紧急情况标记
    boolean isDanger;
    //运行标记
    boolean isMoving;
    //飞行控制线程
    FlightThread thread;

    //飞行控制器实例
    DJIFlightController fc;

    //机体位置，相对起飞点高度及经纬度
    DJIFlightControllerDataType.DJILocationCoordinate3D location;

    //机体朝向，向北为0，顺时针增加,单位弧度
    double heading;
    double lastHeading;
    double lastTime;

    //机体坐标系下，飞行器的速度。
    double velocityX;
    double velocityY;
    double velocityZ;
    double velocityAngle;

    //飞行时间
    float flyTime;

    //超声波高度，-1代表超声波传感器不可用。
    float ultrasonicHeight;

    public void startUp (final DJIFlightController fc) {
        this.fc = fc;
        //设置系统状态改变回调
        fc.setUpdateSystemStateCallback(new DJIFlightControllerDelegate.FlightControllerUpdateSystemStateCallback() {
            @Override
            public void onResult(DJIFlightControllerDataType.DJIFlightControllerCurrentState state) {
                //取得机体位置
                location = state.getAircraftLocation();
                //取得机体朝向并通过坐标变换得到机体坐标系下飞行器速度。
                heading = fc.getCompass().getHeading() / 180 * Math.PI;
                velocityX = state.getVelocityX() * Math.cos(heading) + state.getVelocityY() * Math.cos(Math.PI/2 - heading);
                velocityY = state.getVelocityX() * Math.cos(Math.PI/2 + heading) + state.getVelocityY() * Math.cos(heading);
                velocityZ = state.getVelocityZ();
                if(heading < 0) heading += 2 * Math.PI;
                velocityAngle = Math.abs(heading - lastHeading) > Math.PI ? (Math.PI * 2 - Math.abs(heading - lastHeading)) * Math.signum(lastHeading - heading) : heading - lastHeading;
                velocityAngle /= (System.currentTimeMillis() - lastTime) / 1000f;
                //取得飞行时间
                flyTime = state.getFlyTime();
                //取得超声波高度
                if(state.isUltrasonicBeingUsed())
                    ultrasonicHeight = state.getUltrasonicHeight();
                else
                    ultrasonicHeight = -1;
                lastHeading = heading;
                lastTime = System.currentTimeMillis();
            }
        });
        //设置飞行控制坐标系
        fc.setHorizontalCoordinateSystem(DJIFlightControllerDataType.DJIVirtualStickFlightCoordinateSystem.Body);
        fc.setRollPitchControlMode(DJIFlightControllerDataType.DJIVirtualStickRollPitchControlMode.Velocity);
        fc.setYawControlMode(DJIFlightControllerDataType.DJIVirtualStickYawControlMode.AngularVelocity);
        fc.setVerticalControlMode(DJIFlightControllerDataType.DJIVirtualStickVerticalControlMode.Velocity);
    }

    public void printfInfo (TextView tv) {
        String Info = "";
        Info += "velocityX = "+ velocityX + "\n";
        Info += "velocityY = "+ velocityY + "\n";
        Info += "velocityZ = "+ velocityZ + "\n";
        Info += "velocityAngle = "+ velocityAngle + "\n";
        Info += "heading = " + Math.toDegrees(heading) + "\n";
        Info += "flyTime = "+ flyTime + "\n";
        Info += "ultrasonicHeight = "+ ultrasonicHeight + "\n";
        tv.setText(Info);
    }

    //初始化运动控制
    public void initFC() {
       // if(!fc.isVirtualStickControlModeAvailable())
        fc.enableVirtualStickControlMode(null);
    }

    //启动运动控制线程
    public void startFC () {
        thread = new FlightThread();
        thread.start();
    }

    //结束运动控制线程
    public void endFC () {
        fc.disableVirtualStickControlMode(null);
    }

    public void ahead(final float distance,final float velocity) {
        //if(!fc.isVirtualStickControlModeAvailable()) return;

        thread.addFlightRunnable(new FlightRunnable(distance,velocity,FlightStyle.AHEAD));
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                float moveDistance = 0;
                //发送摇杆指令
                DJIFlightControllerDataType.DJIVirtualStickFlightControlData data =
                        new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(Math.abs(velocity),0,0,0);
                fc.sendVirtualStickFlightControlData(data, null);
                //以100HZ的频率刷新运动数据
                while(!isDanger && moveDistance < distance) {
                    moveDistance -= velocityX * 0.01;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {

                    }
                }
                //恢复悬停状态
                fc.sendVirtualStickFlightControlData(new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(0,0,0,0), null);
            }
        }).start();*/
    }

    public void back(final float distance,final float velocity) {
        //if(!fc.isVirtualStickControlModeAvailable()) return;

        thread.addFlightRunnable(new FlightRunnable(distance,velocity,FlightStyle.BACK));
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                float moveDistance = 0;
                //发送摇杆指令
                DJIFlightControllerDataType.DJIVirtualStickFlightControlData data =
                        new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(-Math.abs(velocity),0,0,0);
                fc.sendVirtualStickFlightControlData(data, null);
                //以100HZ的频率刷新运动数据
                while(!isDanger && moveDistance < distance) {
                    moveDistance += velocityX * 0.01;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {

                    }
                }
                //恢复悬停状态
                fc.sendVirtualStickFlightControlData(new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(0,0,0,0), null);
            }
        }).start();*/
    }

    public  void left(final float distance, final float velocity) {
        //if(!fc.isVirtualStickControlModeAvailable()) return;

        thread.addFlightRunnable(new FlightRunnable(distance,velocity,FlightStyle.LEFT));
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                float moveDistance = 0;
                //发送摇杆指令
                DJIFlightControllerDataType.DJIVirtualStickFlightControlData data =
                        new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(0,-Math.abs(velocity),0,0);
                fc.sendVirtualStickFlightControlData(data, null);
                //以100HZ的频率刷新运动数据
                while(!isDanger && moveDistance < distance) {
                    moveDistance += velocityY * 0.01;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {

                    }
                }
                //恢复悬停状态
                fc.sendVirtualStickFlightControlData(new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(0,0,0,0), null);
            }
        }).start();*/
    }

    public void right(final float distance, final float velocity) {
        //if(!fc.isVirtualStickControlModeAvailable()) return;

        thread.addFlightRunnable(new FlightRunnable(distance,velocity,FlightStyle.RIGHT));
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                float moveDistance = 0;
                //发送摇杆指令
                DJIFlightControllerDataType.DJIVirtualStickFlightControlData data =
                        new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(0,Math.abs(velocity),0,0);
                fc.sendVirtualStickFlightControlData(data, null);
                //以100HZ的频率刷新运动数据
                while(!isDanger && moveDistance < distance) {
                    moveDistance += velocityY * 0.01;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {

                    }
                }
                //恢复悬停状态
                fc.sendVirtualStickFlightControlData(new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(0,0,0,0), null);
            }
        }).start();*/
    }

    public void turnLeft(final float angle, final float angleVelocity) {
        //if(!fc.isVirtualStickControlModeAvailable()) return;

        thread.addFlightRunnable(new FlightRunnable(angle,angleVelocity,FlightStyle.TURNLEFT));
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                float moveAngle = 0;
                double lastHeading = heading;
                //发送摇杆指令
                DJIFlightControllerDataType.DJIVirtualStickFlightControlData data =
                        new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(0,0,-Math.abs(angleVelocity),0);
                fc.sendVirtualStickFlightControlData(data, null);
                //以100HZ的频率刷新运动数据
                while(!isDanger && moveAngle < angle / Math.PI) {
                    moveAngle += lastHeading - heading;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {

                    }
                    lastHeading = heading;
                }
                //恢复悬停状态
                fc.sendVirtualStickFlightControlData(new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(0,0,0,0), null);
            }
        }).start();*/
    }

    public void turnRight(final float angle, final float angleVelocity) {
        //if(!fc.isVirtualStickControlModeAvailable()) return;

        thread.addFlightRunnable(new FlightRunnable(angle,angleVelocity,FlightStyle.TURNRIGHT));
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                float moveAngle = 0;
                double lastHeading = heading;
                //发送摇杆指令
                DJIFlightControllerDataType.DJIVirtualStickFlightControlData data =
                        new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(0,0,Math.abs(angleVelocity),0);
                fc.sendVirtualStickFlightControlData(data, null);
                //以100HZ的频率刷新运动数据
                while(!isDanger && moveAngle < Math.abs(angle / Math.PI)) {
                    moveAngle += heading - lastHeading;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {

                    }
                    lastHeading = heading;
                }
                //恢复悬停状态
                fc.sendVirtualStickFlightControlData(new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(0,0,0,0), null);
            }
        }).start();*/
    }

    public void rise(final float distance, final float velocity) {
        //if(!fc.isVirtualStickControlModeAvailable()) return;

        thread.addFlightRunnable(new FlightRunnable(distance,velocity,FlightStyle.RISE));
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                float moveDistance = 0;
                //发送摇杆指令
                DJIFlightControllerDataType.DJIVirtualStickFlightControlData data =
                        new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(0,0,0,Math.abs(velocity));
                fc.sendVirtualStickFlightControlData(data, null);
                //以100HZ的频率刷新运动数据
                while(!isDanger && moveDistance < distance) {
                    moveDistance += velocityZ * 0.01;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {

                    }
                }
                //恢复悬停状态
                fc.sendVirtualStickFlightControlData(new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(0,0,0,0), null);
            }
        }).start();*/
    }

    public void fall(final float distance, final float velocity) {
        //if(!fc.isVirtualStickControlModeAvailable()) return;

        thread.addFlightRunnable(new FlightRunnable(distance,velocity,FlightStyle.FALL));
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                float moveDistance = 0;
                //发送摇杆指令
                DJIFlightControllerDataType.DJIVirtualStickFlightControlData data =
                        new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(0,0,0,-Math.abs(velocity));
                fc.sendVirtualStickFlightControlData(data, null);
                //以100HZ的频率刷新运动数据
                while(!isDanger && moveDistance < distance) {
                    moveDistance += velocityZ * 0.01;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {

                    }
                }
                //恢复悬停状态
                fc.sendVirtualStickFlightControlData(new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(0,0,0,0), null);
            }
        }).start();*/
    }

    //飞行控制线程
    private class FlightThread extends Thread{

        List<FlightRunnable> runnables;

        public FlightThread () {
            runnables = new ArrayList<FlightRunnable>(5);
        }

        @Override
        public void run() {
            while(true) {
                try {
                    if(runnables.isEmpty())
                        Thread.currentThread().sleep(100);
                    if(isDanger) {
                        endFC();
                        break;
                    }
                } catch (InterruptedException e) {
                }
                runnables.get(0).run();
                runnables.remove(0);
            }
            endFC();
        }

        private  void addFlightRunnable (FlightRunnable runnable) {
            if(runnable != null)
                runnables.add(runnable);
        }
    }

    //飞行控制Runnable
    private class FlightRunnable {

        double nowmove;
        double totalmove;
        //double lastHeading;

        FlightStyle style;

        DJIFlightControllerDataType.DJIVirtualStickFlightControlData data;

        private FlightRunnable (float move,float velocity,FlightStyle style) {
            this.totalmove = move;
            this.style = style;
            switch (style) {
                case AHEAD:
                    this.data = new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(0,Math.abs(velocity),0,0);
                    break;
                case BACK:
                    this.data = new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(0,-Math.abs(velocity),0,0);
                    break;
                case LEFT:
                    this.data = new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(-Math.abs(velocity),0,0,0);
                    break;
                case RIGHT:
                    this.data = new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(Math.abs(velocity),0,0,0);
                    break;
                case TURNLEFT:
                    totalmove = totalmove / 180 * Math.PI;
                    this.data = new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(0,0,-Math.abs(velocity),0);
                    break;
                case TURNRIGHT:
                    totalmove = totalmove / 180 * Math.PI;
                    this.data = new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(0,0,Math.abs(velocity),0);
                    break;
                case RISE:
                    this.data = new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(0,0,0,Math.abs(velocity));
                    break;
                case FALL:
                    this.data = new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(0,0,0,-Math.abs(velocity));
                    break;
            }

        }

        private void run () {
            //以100HZ的频率刷新运动数据,至运动完成
            while(!isDanger && Math.abs(nowmove) < Math.abs(totalmove)) {
                //发送摇杆指令
                fc.sendVirtualStickFlightControlData(data,null);
                switch (style) {
                    case AHEAD:
                        nowmove += velocityX * 0.01;
                        break;
                    case BACK:
                        nowmove -= velocityX * 0.01;
                        break;
                    case LEFT:
                        nowmove -= velocityY * 0.01;
                        break;
                    case RIGHT:
                        nowmove += velocityY * 0.01;
                        break;
                    case TURNLEFT:
                        //nowmove += Math.abs(lastHeading - heading) > Math.PI ? Math.PI * 2 - Math.abs(lastHeading - heading) : lastHeading - heading;
                        nowmove -= velocityAngle * 0.01;
                        break;
                    case TURNRIGHT:
                        //nowmove += Math.abs(heading - lastHeading) > Math.PI ? Math.PI * 2 - Math.abs(heading - lastHeading) : heading - lastHeading;
                        nowmove += velocityAngle * 0.01;
                        break;
                    case RISE:
                        nowmove -= velocityZ * 0.01;
                        break;
                    case FALL:
                        nowmove += velocityZ * 0.01;
                }
                try {
                    Thread.currentThread().sleep(10);
                } catch (InterruptedException e) {
                    fc.sendVirtualStickFlightControlData(new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(0,0,0,0), null);
                }
                //lastHeading = heading;
            }
            //恢复悬停状态
            fc.sendVirtualStickFlightControlData(new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(0,0,0,0), null);
        }

    }

    private enum FlightStyle {
        AHEAD,BACK,LEFT,RIGHT,TURNLEFT,TURNRIGHT,RISE,FALL
    }

}
