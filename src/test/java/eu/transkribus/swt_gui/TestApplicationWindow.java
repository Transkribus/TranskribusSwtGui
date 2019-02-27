package eu.transkribus.swt_gui;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.security.auth.login.LoginException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public abstract class TestApplicationWindow extends ApplicationWindow {
	private static final Logger logger = LoggerFactory.getLogger(TestApplicationWindow.class);

	Storage store = null;

	/**
	 * Create TestAppWindow without Storage
	 */
	public TestApplicationWindow() {
		this(null, null, null);
	}

	/**
	 * Create TestAppWindow with Storage, connected to serverUrl with given
	 * credentials
	 * 
	 * @param serverUrl
	 * @param user
	 * @param pw
	 * 
	 * @throws IllegalStateException including cause if Storage could not be
	 *                               initialized
	 */
	public TestApplicationWindow(String serverUrl, String user, String pw) {
		super(null);
		if (serverUrl != null && user != null && pw != null) {
			try {
				this.initStorage(serverUrl, user, pw);
			} catch (ClientErrorException | ServerErrorException | LoginException | IllegalArgumentException
					| NoConnectionException | InterruptedException | ExecutionException e) {
				throw new IllegalStateException("Could not initialize Storage!", e);
			}
		}
	}

	protected Storage initStorage(String serverUrl, String user, String pw)
			throws ClientErrorException, LoginException, ServerErrorException, IllegalArgumentException,
			NoConnectionException, InterruptedException, ExecutionException {
		store = Storage.getInstance();
		store.login(serverUrl, user, pw);
		Future<?> fut = store.reloadDocList(1); // reload doclist of a collection just that the collection id gets set!
		fut.get();
		return store;
	}
	
	protected Storage getStorage() {
		return store;
	}

	@Override
	protected Control createContents(Composite parent) {
		getShell().setSize(500, 500);
		SWTUtil.centerShell(getShell());
		try {
			createTestContents(parent);
		} catch (Exception e) {
			throw new IllegalStateException("Could not create test content: " + e.getMessage(), e);
		}
		return parent;
	}

	/**
	 * Add your test content here
	 * 
	 * @param parent
	 * @throws Exception
	 */
	protected abstract void createTestContents(Composite parent) throws Exception;

	public void show() {
		try {
			this.setBlockOnOpen(true);
			this.open();
			Display.getCurrent().dispose();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (store != null) {
				store.logout();
			}
		}
	}
}
