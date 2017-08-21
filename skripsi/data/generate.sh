# Generate training plot
cd pelatihan
python plot-pelatihan.py run8-accuracy-train.csv run8-accuracy-test.csv run8-accuracy.png
python plot-pelatihan.py run17-accuracy-train.csv run17-accuracy-test.csv run17-accuracy.png
python plot-pelatihan.py run34-accuracy-train.csv run34-accuracy-test.csv run34-accuracy.png

# Plot sample of sensor data
cd ../plot-sensor
python plot.py ../../../har/sample/train/mobiact/JOG/JOG_2_1.csv berlari.png
python plot.py ../../../har/sample/train/mobiact/SCH/SCH_2_1.csv duduk.png
python plot.py ../../../har/sample/train/mobiact/STD/STD_2_1.csv berdiri.png
python plot.py ../../../har/sample/train/mobiact/STN/STN_2_1.csv turun-tangga.png
python plot.py ../../../har/sample/train/mobiact/STU/STU_2_1.csv naik-tangga.png
python plot.py ../../../har/sample/train/mobiact/WAL/WAL_2_1.csv berjalan.png

# Generate prediction accuracy table and confusion matrix from prediction logs
cd ../prediksi
python summarize.py prediction
python summarize.py confusion_matrix
