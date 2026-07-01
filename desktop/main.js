const { app, BrowserWindow } = require("electron");
const { spawn } = require("child_process");
const path = require("path");
const http = require("http");
const fs = require("fs");

const BACKEND_URL = "http://localhost:8080/";
const BACKEND_READY_TIMEOUT_MS = 30000;

let backendProcess = null;
let mainWindow = null;

function resourcePath(...segments) {
	const base = app.isPackaged ? process.resourcesPath : __dirname;
	return path.join(base, ...segments);
}

function resolveJavaBinary() {
	if (!app.isPackaged) {
		return "java";
	}
	return resourcePath("app", "jre", "Contents", "Home", "bin", "java");
}

function resolveBackendJar() {
	if (!app.isPackaged) {
		return path.join(__dirname, "..", "build", "libs", "pm-management-tool-0.0.1-SNAPSHOT.jar");
	}
	return resourcePath("app", "backend.jar");
}

function startBackend() {
	const javaBin = resolveJavaBinary();
	const jarPath = resolveBackendJar();

	if (!fs.existsSync(jarPath)) {
		throw new Error(`Backend jar not found at ${jarPath}. Run "./gradlew bootJar" first.`);
	}

	const userDataDir = app.getPath("userData");
	fs.mkdirSync(userDataDir, { recursive: true });

	backendProcess = spawn(javaBin, ["-jar", jarPath], {
		cwd: userDataDir,
		stdio: ["ignore", "pipe", "pipe"],
	});

	backendProcess.stdout.on("data", (data) => process.stdout.write(`[backend] ${data}`));
	backendProcess.stderr.on("data", (data) => process.stderr.write(`[backend] ${data}`));
	backendProcess.on("exit", (code) => {
		console.log(`[backend] exited with code ${code}`);
		backendProcess = null;
	});
}

function waitForBackendReady(deadline) {
	return new Promise((resolve, reject) => {
		function attempt() {
			http
				.get(BACKEND_URL, (res) => {
					res.resume();
					resolve();
				})
				.on("error", () => {
					if (Date.now() > deadline) {
						reject(new Error("Timed out waiting for backend to start"));
						return;
					}
					setTimeout(attempt, 300);
				});
		}
		attempt();
	});
}

function createWindow() {
	mainWindow = new BrowserWindow({
		width: 1400,
		height: 900,
		title: "PM Management Tool",
	});
	mainWindow.loadURL(BACKEND_URL);
	mainWindow.on("closed", () => {
		mainWindow = null;
	});
}

function killBackend() {
	if (backendProcess) {
		backendProcess.kill();
		backendProcess = null;
	}
}

const gotSingleInstanceLock = app.requestSingleInstanceLock();
if (!gotSingleInstanceLock) {
	app.quit();
} else {
	app.on("second-instance", () => {
		if (mainWindow) {
			if (mainWindow.isMinimized()) {
				mainWindow.restore();
			}
			mainWindow.focus();
		}
	});

	app.whenReady().then(async () => {
		startBackend();
		try {
			await waitForBackendReady(Date.now() + BACKEND_READY_TIMEOUT_MS);
		} catch (error) {
			console.error(error);
			app.quit();
			return;
		}
		createWindow();
	});

	app.on("window-all-closed", () => {
		killBackend();
		app.quit();
	});

	app.on("before-quit", killBackend);
}
