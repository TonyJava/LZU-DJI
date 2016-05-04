package cn.edu.lzu.oss.lzu_dji;

import dji.sdk.FlightController.DJIFlightController;
import dji.sdk.FlightController.DJIFlightControllerDataType;

/**
 * Created by hopef on 2016/5/3.
 */
public class AdvanceMobileFlyRobot extends MobileFlyRobot{

    //待移动坐标，向前，向右，向下，向右旋转为正
    double restX;
    double restY;
    double restZ;
    double restTurn;

    //移动速度
    double vX;
    double vY;
    double vZ;
    double vAngle;

    //移动速度修正,用于修正设定移动速度与实际移动速度
    double vXFix;
    double vYFix;
    double vZFix;
    double vAngleFix;


    //高级运动线程
    AdvanceFlightThread advanceThread;

    @Override
    public void startUp(DJIFlightController fc) {
        super.startUp(fc);
    }

    @Override
    public void initFC() {
        fc.enableVirtualStickControlMode(null);
        vXFix = 1;
        vYFix = 1;
        vZFix = 1;
        vAngleFix = 1;
    }

    @Override
    public void startFC() {
        advanceThread = new AdvanceFlightThread();
        advanceThread.start();
    }

    @Override
    public void endFC() {
        advanceThread.interrupt();
        fc.disableVirtualStickControlMode(null);
    }

    public void setAhead(double distance) {
        restX = distance;
    }

    public void setBack(double distance) {
        restX = - distance;
    }

    public void setLeft(double distance) {
        restY = - distance;
    }

    public void setRight(double distance) {
        restY = distance;
    }

    public void setRise(double distance) {
        restZ = - distance;
    }

    public void setFall(double distance) {
        restZ = distance;
    }

    public void setTurnLeft(double angle) {
        restTurn = - angle / 180d *Math.PI;
    }

    public void setTurnRight(double angle) {
        restTurn = angle / 180d * Math.PI;
    }

    public void setVX(double v) {
        vX = v;
    }

    public void setVY(double v) {
        vY = v;
    }

    public void setVZ(double v) {
        vZ = v;
    }

    public void setVAngle(double v) {
        vAngle = v;
    }

    private class AdvanceFlightThread extends Thread {

        DJIFlightControllerDataType.DJIVirtualStickFlightControlData data;

        @Override
        public void run() {
            while(true) {
                if(isDanger) break;
                data = new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(0,0,0,0);
                //计算新的速度修正
                if(velocityX != 0 && vX / velocityX < 2)
                    vXFix = vX / velocityX;
                if(velocityY != 0 && vY / velocityY < 2)
                    vYFix = vY / velocityY;
                if(velocityZ != 0 && vZ /velocityZ < 2)
                    vZFix = vZ /velocityZ;
                if(velocityAngle != 0 && vAngle / velocityAngle < 2)
                    vAngleFix = vAngle / velocityAngle;
                //通过修正后的速度计算发送数据
                if(restZ != 0)
                    data = new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(0,0,0,(float) (vZ * vZFix));
                if(restX != 0)
                    data.setRoll((float) (vX * vXFix));
                if(restY != 0)
                    data.setPitch((float) (vY * vYFix));
                if(restTurn != 0)
                    data.setYaw((float) (vAngle * vAngleFix));
                //发送数据
                fc.sendVirtualStickFlightControlData(data,null);

                //处理剩余移动数据
                if(restX < velocityX * 0.01)
                    restX = 0;
                else
                    restX -= velocityX * 0.01;
                if(restY < velocityY * 0.01)
                    restY = 0;
                else
                    restY -= velocityY * 0.01;
                if(restZ < velocityZ * 0.01)
                    restZ = 0;
                else
                    restZ -= velocityZ * 0.01;
                if(restTurn < velocityAngle * 0.01)
                    restTurn = 0;
                else
                    restTurn -= velocityAngle * 0.01;

                try {
                    Thread.currentThread().sleep(10);
                } catch (InterruptedException e) {
                    fc.sendVirtualStickFlightControlData(new DJIFlightControllerDataType.DJIVirtualStickFlightControlData(0,0,0,0),null);
                    endFC();
                }
            }
        }
    }



}
