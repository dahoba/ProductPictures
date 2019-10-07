package eu.formenti.productpictures;

import org.blackdread.camerabinding.jna.EdsdkLibrary;
import org.blackdread.cameraframework.api.camera.CanonCamera;
import org.blackdread.cameraframework.api.command.CanonCommand;
import org.blackdread.cameraframework.api.command.TerminateSdkCommand;
import org.blackdread.cameraframework.api.helper.factory.CanonFactory;
import org.blackdread.cameraframework.api.helper.initialisation.FrameworkInitialisation;
import org.blackdread.cameraframework.api.helper.logic.event.CameraAddedListener;
import org.blackdread.cameraframework.api.helper.logic.event.CameraObjectListener;
import org.blackdread.cameraframework.api.helper.logic.event.CameraPropertyListener;
import org.blackdread.cameraframework.api.helper.logic.event.CameraStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Camera {

    private CanonCamera camera;
    private EdsdkLibrary.EdsCameraRef edsCameraRef;
    private Logger log;
    private static AtomicBoolean propertyEvent = new AtomicBoolean(false);

    Camera() throws Exception {
        this.log = LoggerFactory.getLogger(Main.class);
        CameraAddedListener cameraAddedListener = event -> log.info("Camera added detected: {}", event);
        CameraObjectListener cameraObjectListener = event -> log.info("Object event: {}", event);
        CameraPropertyListener cameraPropertyListener = event -> {
            log.info("Property event: {}", event);
            propertyEvent.set(true);
        };
        CameraStateListener cameraStateListener = event -> log.info("State event: {}", event);

        new FrameworkInitialisation()
                .registerCameraAddedEvent()
                .withEventFetcherLogic()
                .withCameraAddedListener(cameraAddedListener)
                .initialize();

        this.camera = new CanonCamera();
        this.edsCameraRef = get(camera.openSession());
        get(camera.getEvent().registerObjectEventCommand());
        get(camera.getEvent().registerPropertyEventCommand());
        get(camera.getEvent().registerStateEventCommand());
        CanonFactory.cameraObjectEventLogic().addCameraObjectListener(cameraObjectListener);
        CanonFactory.cameraPropertyEventLogic().addCameraPropertyListener(edsCameraRef, cameraPropertyListener);
        CanonFactory.cameraStateEventLogic().addCameraStateListener(edsCameraRef, cameraStateListener);
        propertyEvent.set(false);
        get(camera.getLiveView().beginLiveViewAsync());
        while (!propertyEvent.get()) {
            Thread.sleep(100);
        }
        while (!get(camera.getLiveView().isLiveViewActiveAsync())) {
            Thread.sleep(500);
        }
        Thread.sleep(200);
    }

    public void close() {
        get(camera.getLiveView().endLiveViewAsync());
        get(camera.closeSession());
        CanonFactory.commandDispatcher().scheduleCommand(new TerminateSdkCommand());
    }

    BufferedImage takePicture() {
        return get(camera.getLiveView().downloadLiveViewAsync());
    }

    private static <R> R get(final CanonCommand<R> command) {
        try {
            return command.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }
}
