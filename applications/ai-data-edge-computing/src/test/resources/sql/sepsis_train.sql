SELECT * FROM pgml.train(
        project_name => 'CKD Detection Classifier',
        task => 'classification',
        relation_name => 'ckd_patient_data',
        y_column_name => 'has_ckd',
        algorithm => 'xgboost'
              );