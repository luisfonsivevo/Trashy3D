/*
 * Copyright 2014-2017 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kotcrab.vis.ui.widget;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.kotcrab.vis.ui.FocusManager;

import jerbear.util2d.dialog.Dialog;

/**
 * Compatible with {@link SelectBox}. Does not provide additional features however for proper VisUI focus management VisSelectBox
 * should be always preferred.
 * @author Kotcrab
 * @see SelectBox
 */
public class VisSelectBox<T> extends SelectBox<T> {
	public VisSelectBox (SelectBoxStyle style) {
		super(style);
		init();
	}

	public VisSelectBox (String styleName) {
		super(Dialog.getSkin(), styleName);
		init();
	}

	public VisSelectBox () {
		super(Dialog.getSkin());
		init();
	}

	private void init () {
		addListener(new InputListener() {
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				FocusManager.resetFocus(getStage());
				return false;
			}
		});
	}

}
