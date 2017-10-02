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

package com.kotcrab.vis.ui.util.dialog;

/**
 * Used to get events from {@link Dialogs} option dialog.
 * @author Kotcrab
 */
public interface OptionDialogListener {
	/** Called when 'yes' button was pressed. */
	void yes ();

	/** Called when 'no' button was pressed. */
	void no ();

	/** Called when 'cancel' button was pressed. */
	void cancel ();
}
