import { defineRailway, github, project, service, volume } from "railway/iac";

// The Railway project the app is deployed to. Preview a change with `railway config plan`, apply it with
// `railway config apply`. Railway builds code/Dockerfile and redeploys on every merge to main.
export default defineRailway(() => {
  // The JSON files the app stores its data in. A volume keeps them across redeploys and restarts
  const data = volume("gym-data", { sizeMB: 500 });

  const gym = service("gym-membership", {
    source: github("CS6451-Advanced-Software-Design/Gym-Membership", { branch: "main", rootDirectory: "/code" }),
    build: {
      builder: "DOCKERFILE",
      dockerfilePath: "Dockerfile",
      watchPatterns: ["/code/src/main/**", "/code/pom.xml", "/code/Dockerfile"],
    },
    healthcheck: "/actuator/health",
    healthcheckTimeout: 120,
    volumeMounts: { "/data": data },
  });

  return project("gym-membership", {
    variables: { managed: true },
    resources: [gym, data],
  });
});
