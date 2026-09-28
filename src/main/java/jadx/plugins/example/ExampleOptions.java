package jadx.plugins.example;

import jadx.api.plugins.options.impl.BasePluginOptionsBuilder;

public class ExampleOptions extends BasePluginOptionsBuilder {

	private boolean enable;
	private boolean orderingBugDemo;

	@Override
	public void registerOptions() {
		boolOption(JadxExamplePlugin.PLUGIN_ID + ".enable")
				.description("enable comment")
				.defaultValue(true)
				.setter(v -> enable = v);
		boolOption(JadxExamplePlugin.PLUGIN_ID + ".orderingBugDemo")
				.description("register interdependent passes that reproduce the PassMerge TimSort crash")
				.defaultValue(false)
				.setter(v -> orderingBugDemo = v);
	}

	public boolean isEnable() {
		return enable;
	}

	public boolean isOrderingBugDemo() {
		return orderingBugDemo;
	}
}
