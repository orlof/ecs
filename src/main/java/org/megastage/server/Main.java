package org.megastage.server;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.esotericsoftware.minlog.Log;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.megastage.ecs.ECSWorld;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.megastage.ecs.DependencyInjector.inject;
import static org.megastage.ecs.DependencyInjector.instantiateSingletons;

public class Main {
    @Parameter(names="log_level")
    private int logLevel = Log.LEVEL_INFO;

    @Parameter(names="config")
    private String configFilename = "world.xml";

    private ECSWorld world;


    public static void main(String[] args) throws JDOMException, IOException {
        Main main = new Main();

        JCommander.newBuilder()
                .addObject(main)
                .build()
                .parse(args);

        Log.set(main.logLevel);

        // - instantiates classes annotated with "Singleton"
        Map<Class, Object> singletons = instantiateSingletons("org.megastage");

        // - injects instances to singletons' fields annotated with "Inject"
        List<Object> instances = inject(singletons);

        main.serve();
    }

    void serve() throws JDOMException, IOException {
        world = new ECSWorld(1000);

        world.addSystem(new DcpuSystem(world, 0));

        Element root = readConfig(configFilename);
        for(Element template: root.getChildren("template")) {
            world.addEntityTemplate(template);
        }

        if(!loadSavedWorld()) {
            initializeNewWorld(root);
        }

        loopForever();
    }

    private void loopForever() {

    }

    private void initializeNewWorld(Element root) {

    }

    private boolean loadSavedWorld() {
        return false;
    }

    private Element readConfig(String fileName) throws JDOMException, IOException {
        return new SAXBuilder().build(new File(fileName)).getRootElement();
    }
}
