package blueprint;

import com.vp.plugin.ApplicationManager;
import com.vp.plugin.DiagramManager;
import com.vp.plugin.VPPlugin;
import com.vp.plugin.VPPluginCommandLineSupport;
import com.vp.plugin.VPPluginInfo;
import com.vp.plugin.diagram.IDiagramUIModel;
import com.vp.plugin.diagram.IShapeUIModel;
import com.vp.plugin.diagram.shape.IClassUIModel;
import com.vp.plugin.diagram.shape.IPackageUIModel;
import com.vp.plugin.model.IComponent;
import com.vp.plugin.model.IModelElement;
import com.vp.plugin.model.INOTE;
import com.vp.plugin.model.INode;
import com.vp.plugin.model.IPackage;
import com.vp.plugin.model.IProject;
import com.vp.plugin.model.IRelationship;
import com.vp.plugin.model.ISimpleRelationship;
import com.vp.plugin.model.factory.IModelElementFactory;
import java.awt.Point;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Draws the recovered blueprints from the reversed model: the package diagram with one edge per package import in the
// code, the component diagram built on the real port interfaces, and the deployment diagram
public class Blueprint implements VPPlugin, VPPluginCommandLineSupport {
    private static final String PACKAGE_DIAGRAM = "Recovered package diagram";
    private static final String COMPONENT_DIAGRAM = "Component diagram";
    private static final String DEPLOYMENT_DIAGRAM = "Deployment diagram";

    private final DiagramManager diagrams = ApplicationManager.instance().getDiagramManager();
    private final IModelElementFactory factory = IModelElementFactory.instance();
    private final Map<String, IShapeUIModel> shapes = new HashMap<>();
    private IProject project;
    private IPackage blueprints;

    public void loaded(VPPluginInfo info) {}

    public void unloaded() {}

    public void invoke(String[] args) {
        try {
            project = ApplicationManager.instance().getProjectManager().getProject();
            List<String> edges = Files.readAllLines(Path.of(args[0]));
            blueprints = factory.createPackage();
            blueprints.setName("blueprints");
            drawPackages(edges);
            drawComponents();
            drawDeployment();
            ApplicationManager.instance().getProjectManager().saveProject();
            System.out.println("BLUEPRINT done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private IDiagramUIModel diagram(String type, String name) {
        for (IDiagramUIModel old : project.toDiagramArray()) {
            if (name.equals(old.getName())) {
                old.delete();
            }
        }
        shapes.clear();
        IDiagramUIModel diagram = diagrams.createDiagram(type);
        diagram.setName(name);
        return diagram;
    }

    private void drawPackages(List<String> edges) {
        IDiagramUIModel diagram = diagram(DiagramManager.DIAGRAM_TYPE_PACKAGE_DIAGRAM, PACKAGE_DIAGRAM);

        place(diagram, "GymApplication", 20, 40, 170, 40);
        place(diagram, "OpenApiConfiguration", 20, 100, 170, 40);
        place(diagram, "controller", 230, 40, 150, 70);
        place(diagram, "dto", 20, 200, 150, 70);
        IShapeUIModel port = place(diagram, "port", 210, 160, 390, 140);
        nest(port, place(diagram, "port.in", 230, 210, 150, 70));
        nest(port, place(diagram, "port.out", 430, 210, 150, 70));
        place(diagram, "service", 345, 380, 110, 70);
        place(diagram, "InterceptorConfiguration", 760, 50, 190, 40);
        place(diagram, "interceptor", 1080, 170, 150, 70);
        IShapeUIModel adapter = place(diagram, "adapter", 650, 360, 580, 140);
        nest(adapter, place(diagram, "adapter.persistence", 665, 410, 130, 70));
        nest(adapter, place(diagram, "adapter.payment", 805, 410, 130, 70));
        nest(adapter, place(diagram, "adapter.notification", 945, 410, 130, 70));
        nest(adapter, place(diagram, "adapter.logging", 1085, 410, 130, 70));
        place(diagram, "BillingConfiguration", 470, 515, 170, 40);
        place(diagram, "domain", 20, 600, 1210, 70);

        Map<String, Point[]> routes = Map.of(
                "controller domain", new Point[] {new Point(260, 110), new Point(192, 140), new Point(192, 580), new Point(192, 600)});
        for (String line : edges) {
            String[] parts = line.trim().split(" ");
            String from = parts[0];
            String to = parts[1];
            if (realizes(element(from), element(to))) {
                // Same direction as Instant Reverse: from the interfaces' package to the implementing one
                IRelationship relationship = factory.createRealization();
                relationship.setFrom(element(to));
                relationship.setTo(element(from));
                diagrams.createConnector(diagram, relationship, shapes.get(to), shapes.get(from), routes.get(from + " " + to));
            } else {
                IRelationship relationship = factory.createDependency();
                relationship.setFrom(element(from));
                relationship.setTo(element(to));
                diagrams.createConnector(diagram, relationship, shapes.get(from), shapes.get(to), routes.get(from + " " + to));
            }
        }
    }

    private IShapeUIModel place(IDiagramUIModel diagram, String name, int x, int y, int width, int height) {
        IShapeUIModel shape = (IShapeUIModel) diagrams.createDiagramElement(diagram, element(name));
        shape.setBounds(x, y, width, height);
        if (shape instanceof IPackageUIModel) {
            ((IPackageUIModel) shape).setShowOwnerOption(IPackageUIModel.SHOW_OWNER_NONE);
        }
        hideMembers(shape);
        shape.resetCaption();
        shapes.put(name, shape);
        return shape;
    }

    private void nest(IShapeUIModel parent, IShapeUIModel child) {
        parent.addChild(child);
    }

    private void drawComponents() {
        IDiagramUIModel diagram = diagram(DiagramManager.DIAGRAM_TYPE_COMPONENT_DIAGRAM, COMPONENT_DIAGRAM);

        add(diagram, "postman", component("Postman / Swagger UI", "external"), 20, 130, 170, 70);
        add(diagram, "clock", component("Spring scheduler", "external"), 20, 455, 170, 70);
        add(diagram, "http", created("HTTP API"), 225, 145, 130, 40);
        add(diagram, "rest", component("BillingController, MembershipController", "driving adapter"), 390, 115, 250, 100);
        add(diagram, "jobs", component("BillingJob, MembershipChecksJob", "driving adapter"), 390, 450, 250, 80);
        add(diagram, "membership", component("MembershipService", "use case"), 960, 100, 190, 90);
        add(diagram, "billing", component("BillingService", "use case"), 960, 520, 190, 90);
        String[] memberships = {"SignUpUseCase", "ViewMembershipUseCase", "FreezeMembershipUseCase", "CancelMembershipUseCase",
                "RestoreMembershipUseCase", "RunDailyChecksUseCase"};
        for (int i = 0; i < memberships.length; i++) {
            add(diagram, memberships[i], element("port.in." + memberships[i]), 690, 20 + i * 50, 210, 40);
        }
        add(diagram, "RunMonthlyBillingUseCase", element("port.in.RunMonthlyBillingUseCase"), 690, 470, 210, 40);
        String[] billings = {"CalculateChargesUseCase", "RecordChargeUseCase", "PayArrearsUseCase", "ViewInvoicesUseCase"};
        for (int i = 0; i < billings.length; i++) {
            add(diagram, billings[i], element("port.in." + billings[i]), 950, 240 + i * 60, 210, 40);
        }
        String[] repositories = {"MemberRepository", "MembershipRepository", "ChargeRepository", "InvoiceRepository"};
        for (int i = 0; i < repositories.length; i++) {
            add(diagram, repositories[i], element("port.out." + repositories[i]), 1230, 30 + i * 55, 190, 40);
        }
        add(diagram, "NotificationSender", element("port.out.NotificationSender"), 1230, 330, 190, 40);
        add(diagram, "PaymentGateway", element("port.out.PaymentGateway"), 1230, 470, 190, 40);
        add(diagram, "json", component("JsonFileMemberRepository, JsonFileMembershipRepository, JsonFileChargeRepository, "
                + "JsonFileInvoiceRepository", "driven adapter"), 1480, 40, 230, 180);
        add(diagram, "notification", component("EventNotificationSender", "driven adapter"), 1480, 310, 230, 80);
        add(diagram, "payment", component("FakePaymentGateway", "driven adapter"), 1480, 450, 230, 80);
        add(diagram, "dispatcher", component("UseCaseDispatcher, UseCaseInterception", "interceptor"), 390, 680, 250, 90);
        add(diagram, "UseCaseInterceptor", element("interceptor.UseCaseInterceptor"), 690, 705, 210, 40);
        add(diagram, "logging", component("LoggingInterceptor", "driven adapter"), 960, 680, 190, 90);
        note(diagram, "Every component also uses gym.domain, not drawn. UseCaseDispatcher wraps every port.in use case in a "
                + "dynamic proxy that calls the interceptors, wired by InterceptorConfiguration.", 1230, 660, 480, 90);

        provides(diagram, "rest", "http");
        requires(diagram, "postman", "http");
        for (String useCase : memberships) {
            provides(diagram, "membership", useCase);
        }
        for (String useCase : billings) {
            provides(diagram, "billing", useCase);
            requires(diagram, "membership", useCase);
        }
        provides(diagram, "billing", "RunMonthlyBillingUseCase");
        for (String useCase : new String[] {"SignUpUseCase", "ViewMembershipUseCase", "FreezeMembershipUseCase",
                "CancelMembershipUseCase", "RestoreMembershipUseCase", "RunDailyChecksUseCase", "RunMonthlyBillingUseCase",
                "ViewInvoicesUseCase"}) {
            requires(diagram, "rest", useCase);
        }
        requires(diagram, "jobs", "RunMonthlyBillingUseCase");
        requires(diagram, "jobs", "RunDailyChecksUseCase");
        requires(diagram, "clock", "jobs");
        for (String repository : repositories) {
            provides(diagram, "json", repository);
            requires(diagram, "billing", repository);
        }
        requires(diagram, "membership", "MemberRepository");
        requires(diagram, "membership", "MembershipRepository");
        requires(diagram, "membership", "NotificationSender");
        requires(diagram, "billing", "NotificationSender");
        requires(diagram, "billing", "PaymentGateway");
        provides(diagram, "notification", "NotificationSender");
        provides(diagram, "payment", "PaymentGateway");
        provides(diagram, "logging", "UseCaseInterceptor");
        requires(diagram, "dispatcher", "UseCaseInterceptor");
    }

    private void drawDeployment() {
        IDiagramUIModel diagram = diagram(DiagramManager.DIAGRAM_TYPE_DEPLOYMENT, DEPLOYMENT_DIAGRAM);

        IShapeUIModel laptop = add(diagram, "laptop", node("Client machine", "device"), 20, 60, 260, 230);
        laptop.addChild(add(diagram, "postman", artifact("Postman (live, mock and local environments)"), 40, 120, 220, 60));
        laptop.addChild(add(diagram, "browser", artifact("Web browser (Swagger UI)"), 40, 210, 220, 50));
        add(diagram, "mock", node("Postman mock server", "cloud service"), 20, 400, 260, 80);
        add(diagram, "github", node("GitHub", "cloud service"), 380, 470, 330, 120);
        shapes.get("github").addChild(add(diagram, "actions", artifact("GitHub Actions CI: unit, integration and end-to-end tests"), 400, 520, 290, 50));

        IShapeUIModel railway = add(diagram, "railway", node("Railway (region iad)", "PaaS"), 380, 20, 900, 400);
        railway.addChild(add(diagram, "proxy", node("Railway HTTPS proxy (TLS ends here)", "edge proxy"), 400, 80, 200, 90));
        IShapeUIModel container = add(diagram, "container", node("Docker container: eclipse-temurin:21-jre-noble",
                "execution environment"), 640, 70, 360, 320);
        railway.addChild(container);
        IShapeUIModel jvm = add(diagram, "jvm", node("JVM 21 (-XX:MaxRAMPercentage=75)", "execution environment"), 660, 130, 320, 160);
        container.addChild(jvm);
        jvm.addChild(add(diagram, "jar", artifact("gym-membership.jar (Spring Boot, embedded Tomcat on $PORT)"), 680, 190, 280, 70));
        container.addChild(add(diagram, "dockerfile", artifact("Dockerfile: JDK 21 build stage, JRE 21 run stage"), 660, 310, 320, 50));
        IShapeUIModel data = add(diagram, "volume", node("Railway volume gym-data (500 MB), mounted at /data", "volume"), 1030, 70, 230, 320);
        railway.addChild(data);
        String[] files = {"members.json", "memberships.json", "charges.json", "invoices.json"};
        for (int i = 0; i < files.length; i++) {
            data.addChild(add(diagram, files[i], artifact(files[i]), 1050, 140 + i * 60, 190, 45));
        }

        path(diagram, "laptop", "proxy", "HTTPS");
        path(diagram, "proxy", "jar", "HTTP on $PORT");
        path(diagram, "jar", "volume", "file I/O, GYM_PERSISTENCE_DIRECTORY=/data");
        path(diagram, "laptop", "mock", "HTTPS");
        path(diagram, "github", "railway", "deploys main once CI passes");
        note(diagram, "Railway builds code/Dockerfile, checks /actuator/health, and keeps the JSON files on the volume across "
                + "redeploys. Logs are JSON (logstash format).", 760, 470, 520, 70);
    }

    private IShapeUIModel add(IDiagramUIModel diagram, String key, IModelElement model, int x, int y, int width, int height) {
        IShapeUIModel shape = (IShapeUIModel) diagrams.createDiagramElement(diagram, model);
        shape.setBounds(x, y, width, height);
        hideMembers(shape);
        shape.resetCaption();
        shapes.put(key, shape);
        return shape;
    }

    private void hideMembers(IShapeUIModel shape) {
        if (shape instanceof IClassUIModel) {
            // Index 0 of SHOW_ATTRIBUTE_OPTIONS and SHOW_OPERATION_OPTIONS is "Hide all"
            ((IClassUIModel) shape).setShowAttributeType(0);
            ((IClassUIModel) shape).setShowOperationType(0);
            ((IClassUIModel) shape).setShowEmptyCompartments(2);
        }
    }

    private IModelElement component(String name, String stereotype) {
        IComponent component = factory.createComponent();
        component.setName(name);
        component.addStereotype(stereotype);
        blueprints.addChild(component);
        return component;
    }

    private IModelElement node(String name, String stereotype) {
        INode node = factory.createNode();
        node.setName(name);
        node.addStereotype(stereotype);
        blueprints.addChild(node);
        return node;
    }

    private IModelElement artifact(String name) {
        IModelElement artifact = factory.createArtifact();
        artifact.setName(name);
        blueprints.addChild(artifact);
        return artifact;
    }

    // An interface the code has no type for, modelled the way Instant Reverse models Java interfaces
    private IModelElement created(String name) {
        IModelElement element = factory.createClass();
        element.addStereotype("Interface");
        element.setName(name);
        blueprints.addChild(element);
        return element;
    }

    private void provides(IDiagramUIModel diagram, String provider, String contract) {
        IRelationship realization = factory.createRealization();
        realization.setFrom(shapes.get(contract).getModelElement());
        realization.setTo(shapes.get(provider).getModelElement());
        diagrams.createConnector(diagram, realization, shapes.get(contract), shapes.get(provider), null);
    }

    private void requires(IDiagramUIModel diagram, String client, String supplier) {
        IRelationship usage = factory.createUsage();
        usage.setFrom(shapes.get(client).getModelElement());
        usage.setTo(shapes.get(supplier).getModelElement());
        diagrams.createConnector(diagram, usage, shapes.get(client), shapes.get(supplier), null);
    }

    private void path(IDiagramUIModel diagram, String from, String to, String name) {
        IRelationship association = factory.createAssociation();
        association.setName(name);
        association.setFrom(shapes.get(from).getModelElement());
        association.setTo(shapes.get(to).getModelElement());
        diagrams.createConnector(diagram, association, shapes.get(from), shapes.get(to), null).resetCaption();
    }

    private void note(IDiagramUIModel diagram, String text, int x, int y, int width, int height) {
        INOTE note = factory.createNOTE();
        note.setDescription(text);
        IShapeUIModel shape = (IShapeUIModel) diagrams.createDiagramElement(diagram, note);
        shape.setBounds(x, y, width, height);
    }

    // Finds gym.<name> in the reversed model, where name is a dotted package path or a root class
    private IModelElement element(String name) {
        IModelElement current = null;
        for (IModelElement root : project.toModelElementArray()) {
            if ("gym".equals(root.getName())) {
                current = root;
            }
        }
        for (String part : name.split("\\.")) {
            IModelElement next = null;
            for (IModelElement child : current.toChildArray()) {
                if (part.equals(child.getName())) {
                    next = child;
                }
            }
            if (next == null) {
                throw new IllegalArgumentException("Not in the model: " + name);
            }
            current = next;
        }
        return current;
    }

    // True when a class inside from realizes an interface inside to, as the reversed model records it
    private boolean realizes(IModelElement from, IModelElement to) {
        for (IModelElement type : types(from)) {
            // Instant Reverse stores a realization from the interface to the class that implements it
            for (ISimpleRelationship relationship : type.toToRelationshipArray()) {
                if ("Realization".equals(relationship.getModelType())
                        && relationship.getTo().getId().equals(type.getId())
                        && contains(to, relationship.getFrom())) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<IModelElement> types(IModelElement element) {
        if (!"Package".equals(element.getModelType())) {
            return List.of(element);
        }
        return List.of(element.toChildArray("Class"));
    }

    private boolean contains(IModelElement container, IModelElement element) {
        for (IModelElement parent = element; parent != null; parent = parent.getParent()) {
            if (parent == container || parent.getId().equals(container.getId())) {
                return true;
            }
        }
        return false;
    }
}
