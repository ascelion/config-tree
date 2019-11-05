package ascelion.config.microprofile;

final class Mediator {

	private final ThreadLocal<Boolean> acquired = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		};
	};

	boolean acquire() {
		if (this.acquired.get()) {
			return false;
		}

		this.acquired.set(true);

		return true;
	}

	void release() {
		this.acquired.remove();
	}
}
