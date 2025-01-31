/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.internal.widget.remotecompose.core;

import static com.android.internal.widget.remotecompose.core.RemoteContext.ID_CONTINUOUS_SEC;
import static com.android.internal.widget.remotecompose.core.RemoteContext.ID_TIME_IN_MIN;
import static com.android.internal.widget.remotecompose.core.RemoteContext.ID_TIME_IN_SEC;
import static com.android.internal.widget.remotecompose.core.RemoteContext.ID_WINDOW_HEIGHT;
import static com.android.internal.widget.remotecompose.core.RemoteContext.ID_WINDOW_WIDTH;

import com.android.internal.widget.remotecompose.core.operations.utilities.IntFloatMap;
import com.android.internal.widget.remotecompose.core.operations.utilities.IntIntMap;
import com.android.internal.widget.remotecompose.core.operations.utilities.IntMap;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents runtime state for a RemoteCompose document
 * State includes things like the value of variables
 */
public class RemoteComposeState {
    public static final int START_ID = 42;
    private static final int MAX_FLOATS = 500;
    private static final int MAX_COLORS = 200;
    private final IntMap<Object> mIntDataMap = new IntMap<>();
    private final IntMap<Boolean> mIntWrittenMap = new IntMap<>();
    private final HashMap<Object, Integer> mDataIntMap = new HashMap();
    private final IntFloatMap mFloatMap = new IntFloatMap(); // efficient cache
    private final IntIntMap mIntegerMap = new IntIntMap(); // efficient cache
    private final IntIntMap mColorMap = new IntIntMap(); // efficient cache
    private final boolean[] mColorOverride = new boolean[MAX_COLORS];
    private int mNextId = START_ID;


    /**
     * Get Object based on id. The system will cache things like bitmaps
     * Paths etc. They can be accessed with this command
     *
     * @param id
     * @return
     */
    public Object getFromId(int id) {
        return mIntDataMap.get(id);
    }

    /**
     * true if the cache contain this id
     *
     * @param id
     * @return
     */
    public boolean containsId(int id) {
        return mIntDataMap.get(id) != null;
    }

    /**
     * Return the id of an item from the cache.
     */
    public int dataGetId(Object image) {
        Integer res = mDataIntMap.get(image);
        if (res == null) {
            return -1;
        }
        return res;
    }

    /**
     * Add an image to the cache. Generates an id for the image and adds it to the cache based on
     * that id.
     */
    public int cache(Object image) {
        int id = nextId();
        mDataIntMap.put(image, id);
        mIntDataMap.put(id, image);
        return id;
    }

    /**
     * Insert an item in the cache
     */
    public void cache(int id, Object item) {
        mDataIntMap.put(item, id);
        mIntDataMap.put(id, item);
    }

    /**
     * Insert an item in the cache
     */
    public void update(int id, Object item) {
        mDataIntMap.remove(mIntDataMap.get(id));
        mDataIntMap.put(item, id);
        mIntDataMap.put(id, item);
    }

    /**
     * Insert an item in the cache
     */
    public int cacheFloat(float item) {
        int id = nextId();
        mFloatMap.put(id, item);
        mIntegerMap.put(id, (int) item);
        return id;
    }

    /**
     * Insert an item in the cache
     */
    public int cacheInteger(int item) {
        int id = nextId();
        mIntegerMap.put(id, item);
        mFloatMap.put(id, item);
        return id;
    }

    /**
     * Insert an item in the cache
     */
    public void cacheFloat(int id, float item) {
        mFloatMap.put(id, item);
    }

    /**
     * Insert an float item in the cache
     */
    public void updateFloat(int id, float item) {
        mFloatMap.put(id, item);
        mIntegerMap.put(id, (int) item);
    }

    /**
     * Insert an integer item in the cache
     */
    public void updateInteger(int id, int item) {
        mFloatMap.put(id, item);
        mIntegerMap.put(id, item);
    }

    /**
     * get a float from the float cache
     *
     * @param id of the float value
     * @return the float value
     */
    public float getFloat(int id) {
        return mFloatMap.get(id);
    }

    /**
     * get an integer from the cache
     *
     * @param id of the integer value
     * @return the integer
     */
    public int getInteger(int id) {
        return mIntegerMap.get(id);
    }

    /**
     * Get the float value
     *
     * @param id
     * @return
     */
    public int getColor(int id) {
        return mColorMap.get(id);
    }

    /**
     * Modify the color at id.
     *
     * @param id
     * @param color
     */
    public void updateColor(int id, int color) {
        if (mColorOverride[id]) {
            return;
        }
        mColorMap.put(id, color);
    }

    /**
     * Adds a colorOverride.
     * This is a list of ids and there colors optimized for playback;
     *
     * @param id
     * @param color
     */
    public void overrideColor(int id, int color) {
        mColorOverride[id] = true;
        mColorMap.put(id, color);
    }

    /**
     * Clear the color Overrides
     */
    public void clearColorOverride() {
        for (int i = 0; i < mColorOverride.length; i++) {
            mColorOverride[i] = false;
        }
    }

    /**
     * Method to determine if a cached value has been written to the documents WireBuffer based on
     * its id.
     */
    public boolean wasNotWritten(int id) {
        return !mIntWrittenMap.get(id);
    }

    /**
     * Method to mark that a value, represented by its id, has been written to the WireBuffer
     */
    public void markWritten(int id) {
        mIntWrittenMap.put(id, true);
    }

    /**
     * Clear the record of the values that have been written to the WireBuffer.
     */
    void reset() {
        mIntWrittenMap.clear();
    }

    /**
     * Get the next available id
     *
     * @return
     */
    public int nextId() {
        return mNextId++;
    }

    /**
     * Set the next id
     *
     * @param id
     */
    public void setNextId(int id) {
        mNextId = id;
    }

    IntMap<ArrayList<VariableSupport>> mVarListeners = new IntMap<>();
    ArrayList<VariableSupport> mAllVarListeners = new ArrayList<>();

    private void add(int id, VariableSupport variableSupport) {
        ArrayList<VariableSupport> v = mVarListeners.get(id);
        if (v == null) {
            v = new ArrayList<VariableSupport>();
            mVarListeners.put(id, v);
        }
        v.add(variableSupport);
        mAllVarListeners.add(variableSupport);
    }

    /**
     * Commands that listen to variables add themselves.
     *
     * @param id
     * @param variableSupport
     */
    public void listenToVar(int id, VariableSupport variableSupport) {
        add(id, variableSupport);
    }

    /**
     * List of Commands that need to be updated
     *
     * @param context
     * @return
     */
    public int getOpsToUpdate(RemoteContext context) {
        for (VariableSupport vs : mAllVarListeners) {
            vs.updateVariables(context);
        }
        if (mVarListeners.get(ID_CONTINUOUS_SEC) != null) {
            return 1;
        }
        if (mVarListeners.get(ID_TIME_IN_SEC) != null) {
            return 1000;
        }
        if (mVarListeners.get(ID_TIME_IN_MIN) != null) {
            return 1000 * 60;
        }
        return -1;
    }

    /**
     * Set the width of the overall document on screen.
     *
     * @param width
     */
    public void setWindowWidth(float width) {
        updateFloat(ID_WINDOW_WIDTH, width);
    }

    /**
     * Set the width of the overall document on screen.
     *
     * @param height
     */
    public void setWindowHeight(float height) {
        updateFloat(ID_WINDOW_HEIGHT, height);
    }

}
