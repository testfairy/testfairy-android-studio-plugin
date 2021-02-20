package com.testfairy.plugin.intellij;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

class DoNotAskBrowserOption implements DialogWrapper.DoNotAskOption {
	@Override
	public boolean isToBeShown() {
		return true; // True means "Do not show again" checkbox is not selected by default
	}

	@Override
	public void setToBeShown(boolean b, int i) {
		if (!b) { // If b is false, it means checkbox is selected
			Plugin.setShouldLaunchBrowser(i == Messages.OK); // Remember choice
		}
	}

	@Override
	public boolean canBeHidden() {
		// If returns true, checkbox is always shown
		return true;
	}

	@Override
	public boolean shouldSaveOptionsOnCancel() {
		// If returns false, setToBeShown is not called on cancel
		return true;
	}

	@NotNull
	@Override
	public String getDoNotShowMessage() {
		return "Do not ask again";
	}
}
