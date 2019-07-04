import CSV from "./CSV.mjs";
import ParallelCoordinatesPlot from "./ParallelCoordinatesPlot.mjs";
import PixelVisualizationMatrix from "./PixelVisualizationMatrix.mjs";
import ScatterplotMatrix from "./ScatterplotMatrix.mjs";

let currentlyActiveTarget;
let dataSetChanged = false;
let parallelCoordinatesPlot;
let pixelVisualizationMatrix;
let scatterplotMatrix;
let targets = {
  pcp: { dataSetChanged: false },
  pvm: { dataSetChanged: false },
  splom: { dataSetChanged: false }
};

const csv = new CSV();

const configureDatasets = () => {
  const dataSets = [
    { element: "#csv_singleSteps", fileName: "csv_singleSteps.csv" },
    {
      element: "#education-dataset",
      fileName: "education_and_related_statistics_states_no_label.csv"
    },
    { element: "#iris-dataset", fileName: "iris_with_label.csv" },
    { element: "#nutrients-dataset", fileName: "nutrients_with_label.csv" },
    { element: "#wine-dataset", fileName: "wine_data.csv" }
  ];

  $("#csv-button").on("click", () => {
    $("#csv-input").val(null);
    $("#csv-input").trigger("click");
  });

  $("#csv-input").on("change", async event => {
    await csv.loadCustom(event.target.files[0]);

    currentlyActiveTarget.processDataset(csv);
    currentlyActiveTarget.render();

    Object.values(targets)
      .filter(target => target.target !== currentlyActiveTarget)
      .forEach(target => (target.dataSetChanged = true));
  });

  dataSets.forEach(dataSet =>
    $(dataSet.element).on("click", async () => {
      await csv.loadPredefined(dataSet.fileName);

      currentlyActiveTarget.processDataset(csv);
      currentlyActiveTarget.render();

      Object.values(targets)
        .filter(target => target.target !== currentlyActiveTarget)
        .forEach(target => (target.dataSetChanged = true));
    })
  );
};

const observeTabSwitch = () => {
  const parallelCoordinatesPlotObserver = new MutationObserver(tabChanged);
  const pixelVisualizationMatrixObserver = new MutationObserver(tabChanged);
  const scatterplotMatrixObserver = new MutationObserver(tabChanged);
  const observerConfiguration = {
    attributes: true,
    attributeOldValue: false,
    attributeFilter: ["class"],
    characterData: false,
    characterDataOldValue: false
  };

  parallelCoordinatesPlotObserver.observe(
    document.getElementById("pcp"),
    observerConfiguration
  );
  pixelVisualizationMatrixObserver.observe(
    document.getElementById("pvm"),
    observerConfiguration
  );
  scatterplotMatrixObserver.observe(
    document.getElementById("splom"),
    observerConfiguration
  );
};

const tabChanged = mutationRecords => {
  const target = mutationRecords[0].target;
  const targetCurrentlyActive =
    target.classList.contains("active") && target.classList.contains("show");

  if (targetCurrentlyActive) {
    currentlyActiveTarget = targets[target.id].target;
  }

  if (targets[target.id].dataSetChanged) {
    currentlyActiveTarget.processDataset(csv);
    currentlyActiveTarget.render();

    targets[target.id].dataSetChanged = false;
  }
};

$(document).ready(() => {
  configureDatasets();
  observeTabSwitch();

  parallelCoordinatesPlot = new ParallelCoordinatesPlot();
  pixelVisualizationMatrix = new PixelVisualizationMatrix();
  scatterplotMatrix = new ScatterplotMatrix();

  currentlyActiveTarget = pixelVisualizationMatrix;

  targets.pcp.target = parallelCoordinatesPlot;
  targets.pvm.target = pixelVisualizationMatrix;
  targets.splom.target = scatterplotMatrix;
});
