
import java.io.*;
import javax.sound.sampled.*;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author guille
 */
public class audioPB {

     //global audio variables
     AudioFormat audioFormat;
     AudioInputStream           audioInputStream;
     SourceDataLine                sourceDataLine;
     boolean                stopPlayback = false,repetir =false;
     File soundFile;

     public audioPB(){}
     public void play(File soundFile)
     {
          try{
               this.soundFile = soundFile;
               audioInputStream = AudioSystem.getAudioInputStream(soundFile); //get audio stream
               audioFormat      = audioInputStream.getFormat(); //get format of stream
               
               DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class,audioFormat); //request a data line (not open)
          
               sourceDataLine = (SourceDataLine)AudioSystem.getLine(dataLineInfo); //inform line of data type
               stopPlayback   = false; //allow playback
               new Play_Audio().start(); //play thread
          }
          catch (Exception error)
          {

          }
     }

     /**
      *Play_Audio Class
      */
     private class Play_Audio extends Thread
     {
          byte buffer[] = new byte[10000]; //10KB buffer, will not be too large for any system
          
          /**
           *Thread event which handles audio playback
           */
          public void run()
          {
               try{
                     sourceDataLine.open(audioFormat); //open the line
                     sourceDataLine.start(); //prepare line for data transfer
     
                     int continue_play;
                     while((continue_play = audioInputStream.read(buffer,0,buffer.length)) != -1 && stopPlayback == false)
                     {
                         if(continue_play > 0) //data still left to write
                         {
                              sourceDataLine.write(buffer, 0, continue_play); //write data to the line
                         }
                     }
                     sourceDataLine.drain(); //clear buffer
                     sourceDataLine.close(); //close line
                     if (repetir) play(soundFile);
               }
               catch(Exception e)
               {

               }
          }
     }
    
}
