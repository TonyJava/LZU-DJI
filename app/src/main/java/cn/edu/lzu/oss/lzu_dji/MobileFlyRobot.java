package cn.edu.lzu.oss.lzu_dji;

import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import dji.sdk.FlightController.*;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.base.DJIError;

/**
 * Created by hopef on 2016/4/30.
 */
public class MobileFlyRobot {

    //紧急情况标记
    boolean isDanger;

    //飞行控制器实例
    DJIFlightController fc;

    //机体位置，相对起飞点高度及经纬度
    DJIFlightControllerDataType.DJILocationCoordinate3D location;

    //机体朝向，向北为0，顺时针增加
    double heading;

    //机体坐标系下，飞行器的速度。
    double velocityX;
    double velocityY;
    double velocityZ;

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
                //取得飞行时间
                flyTime = state.getFlyTime();
                //取得超声波高度
                if(state.isUltrasonicBeingUsed())
                    ultrasonicHeight = state.getUltrasonicHeight();
                else
                    ultrasonicHeight = -1;

            }
        });
        //设置飞行控制坐标系
        fc.setHorizontalCoordinateSystem(DJIFlightControllerDataType.DJIVirtualStickFlightCoordinateSystem.Body);
        fc.setRollPitchControlMode(DJIFlightControllerDataType.DJIVirtualStickRollPitchControlMode.Velocity);
        fc.setYawControlMode(DJIFlightControllerDataType.DJIVirtualStickYawControlMode.AngularVelocity);
        fc.setVerticalControlMode(DJIFlightControllerDataType.DJIVirtualStickVerticalControlMode.Velocity);

    }

    public void ahead(final float distance,final float velocity) {
        if(!fc.isVirtualStickControlModeAvailable()) return;

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
        }).start();
    }

    public void back(final float distance,final float velocity) {
        if(!fc.isVirtualStickControlModeAvailable()) return;

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
        }).start();
    }

    public  void left(final float distance, final float velocity) {
        if(!fc.isVirtualStickControlModeAvailable()) return;

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
        }).start();
    }

    public void right(final float distance, final float velocity) {
        if(!fc.isVirtualStickControlModeAvailable()) return;

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
        }).start();
    }

    public void turnLeft(final float angle, final float angleVelocity) {
        if(!fc.isVirtualStickControlModeAvailable()) return;

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
        }).start();
    }

    public void turnRight(final float angle, final float angleVelocity) {
        if(!fc.isVirtualStickControlModeAvailable()) return;

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
        }).start();
    }

    public void rise(final float distance, final float velocity) {
        if(!fc.isVirtualStickControlModeAvailable()) return;

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
        }).start();
    }

    public void fall(final float distance, final float velocity) {
        if(!fc.isVirtualStickControlModeAvailable()) return;

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
        }).start();
    }
}
