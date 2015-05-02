package com.leonziyo.recording;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainGame extends ApplicationAdapter {

    boolean recordTurn = true;

    final int samples = 44100;
    boolean isMono = true, recording = false, playing = false;

    @Override
    public void create () {}

    @Override
    public void render () {
        /*Changing the color just to know when it is done recording or playing audio (optional)*/
        if(recording)
            Gdx.gl.glClearColor(1, 0, 0, 1);
        else if(playing)
            Gdx.gl.glClearColor(0, 1, 0, 1);
        else
            Gdx.gl.glClearColor(0, 0, 1, 1);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // We trigger recording and playing with touch for simplicity
        if(Gdx.input.justTouched()) {
            if(recordTurn)
                recordToFile("sound.bin", 3); //pass file name and number of seconds to record
            else
                playFile("sound.bin"); //file name to play

            recordTurn = !recordTurn;
        }
    }

    @Override
    public void dispose() {

    }

    private void recordToFile(final String filename, final int seconds) {
        //Start a new thread to do the recording, because it will block and render won't be called if done in the main thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    recording = true;

                    short[] data = new short[samples * seconds];
                    AudioRecorder recorder = Gdx.audio.newAudioRecorder(samples, isMono);
                    recorder.read(data, 0, data.length);
                    recorder.dispose();
                    saveAudioToFile(data, filename);
                }
                catch(GdxRuntimeException e) {
                    Gdx.app.log("test", e.getMessage());
                }
                finally {
                    recording = false;
                }
            }
        }).start();

    }

    private void playFile(final String filename) {
        //Start a new thread to play the file, because it will block and render won't be called if done in the main thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    playing = true;
                    short[] data = getAudioDataFromFile(filename); //get audio data from file

                    AudioDevice device = Gdx.audio.newAudioDevice(samples, isMono);
                    device.writeSamples(data, 0, data.length);

                    device.dispose();
                }
                catch(GdxRuntimeException e) {
                    Gdx.app.log("test", e.getMessage());
                }
                finally {
                    playing = false;
                }
            }
        }).start();
    }

    private short[] getAudioDataFromFile(String filename) {
        FileHandle file = Gdx.files.local(filename);
        byte[] temp = file.readBytes(); // get all bytes from file
        short[] data = new short[temp.length / 2]; // create short with half the length (short = 2 bytes)

        ByteBuffer.wrap(temp).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(data); // cast a byte array to short array

        return data;
    }

    private void saveAudioToFile(short[] data, String filename) {
        byte[] temp = new byte[data.length * 2]; //create a byte array to hold the data passed (short = 2 bytes)

        ByteBuffer.wrap(temp).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(data); // cast a short array to byte array

        FileHandle file = Gdx.files.local(filename);
        file.writeBytes(temp, false); //save bytes to file
    }

}
