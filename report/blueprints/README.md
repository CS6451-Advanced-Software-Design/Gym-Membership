# Recovered Blueprints (report §10)

The package, component and deployment diagrams, recovered from the code in Visual Paradigm 18.1.

| File | What it is |
| --- | --- |
| [gym-membership.vpp](gym-membership.vpp) | The Visual Paradigm project: the model reversed from the code, and the three diagrams |
| [package-diagram.png](package-diagram.png), [component-diagram.png](component-diagram.png), [deployment-diagram.png](deployment-diagram.png) | The diagrams, exported from Visual Paradigm Community Edition |
| [edges.txt](edges.txt) | Package dependencies from the `import` lines: from, to, number of imports |
| [plugin/](plugin) | The Visual Paradigm Open API plugin that drew the diagrams on the reversed model (GenAI #38) |

## Rebuilding

Instant Reverse and the command-line plugin runner need Visual Paradigm Standard or above (we used the 30-day
Enterprise evaluation). The scripts that ship in `Visual Paradigm.app/Contents/Resources/app/scripts` pass
`-XX:MaxPermSize`, which the bundled Java 11 rejects, so run their classes with that JVM directly:

```sh
app="/Applications/Visual Paradigm.app/Contents/Resources"
classpath=$(grep -o '\-cp "[^"]*"' "$app/app/scripts/InstantReverse.sh" | sed 's/-cp "//; s/"$//')
vp() { (cd "$app/app/scripts" && "$app/jre.bundle/Contents/Home/bin/java" -Djava.awt.headless=true -cp "$classpath" "com.vp.cmd.$@"); }

# 1. Reverse the bytecode: the Java source parser skips records
(cd code && ./mvnw -B -q compile -Dpmd.skip -Djacoco.skip)
vp InstantReverse -project "$PWD/report/blueprints/gym-membership.vpp" -path "$PWD/code/target/classes" -lang Java -pathtype folder -sourcetype class

# 2. List the package dependencies from the imports (a root class stands for itself)
(cd code/src/main/java && for f in $(find gym -name '*.java' ! -name package-info.java); do
  p=$(dirname "$f" | sed 's|^gym/\{0,1\}||; s|/|.|g'); [ -z "$p" ] && p=$(basename "$f" .java)
  grep -oE '^import gym\.[a-z.]+\.[A-Z]' "$f" | sed -E 's/^import gym\.//; s/\.[A-Z]$//' | sed "s|^|$p |"
done | sort | uniq -c | awk '{print $2, $3, $1}') > report/blueprints/edges.txt

# 3. Draw the diagrams: install the plugin, then run it on the project with the edge list
plugins="$HOME/Library/Application Support/VisualParadigm/plugins/blueprint"
mkdir -p "$plugins/classes" && cp report/blueprints/plugin/plugin.xml "$plugins"
javac --release 11 -cp "$app/app/lib/openapi.jar" -d "$plugins/classes" report/blueprints/plugin/Blueprint.java
vp Plugin -project "$PWD/report/blueprints/gym-membership.vpp" -pluginid blueprint -pluginargs "$PWD/report/blueprints/edges.txt"

# 4. Export from Community Edition: the same command-line runner, with its own JVM, and only a small corner mark
ce="/Applications/Visual Paradigm CE.app/Contents/Resources"
ce_classpath=$(grep -o '\-cp "[^"]*"' "$ce/app/scripts/ExportDiagramImage.sh" | sed 's/-cp "//; s/"$//')
(cd "$ce/app/scripts" && "$ce/jre.bundle/Contents/Home/bin/java" -Djava.awt.headless=true -cp "$ce_classpath" com.vp.cmd.ExportDiagramImage \
  -project "$PWD/report/blueprints/gym-membership.vpp" -out /tmp/blueprints -type png_with_background \
  -diagram "Recovered package diagram" "Component diagram" "Deployment diagram")
```

Then copy the three PNGs over `package-diagram.png`, `component-diagram.png` and `deployment-diagram.png`. `png_with_background` keeps them on white, so they stay readable on GitHub's dark theme. The evaluation's own exports are covered in a watermark.
