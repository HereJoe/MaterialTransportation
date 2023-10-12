# use example
# params: source_name, target_name, path_count=1, use_log=false, print_path=true
CALL findPath4MultiTargetDefault ('Device A','Device G');

DELIMITER //
DROP PROCEDURE IF EXISTS findPath4MultiTargetDefault;
CREATE PROCEDURE findPath4MultiTargetDefault(
	in source_name VARCHAR(50),
	in target_names VARCHAR(255)
)
BEGIN
	CALL findPath4MultiTarget(source_name,target_names,1,FALSE,TRUE);
END //
DELIMITER ;
