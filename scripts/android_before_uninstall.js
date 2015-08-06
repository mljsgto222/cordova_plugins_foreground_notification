
module.exports = function(context){
    var path = context.requireCordovaModule('path'),
        fs = context.requireCordovaModule('fs'),
        projectRoot = context.opts.projectRoot,
        ConfigParser = context.requireCordovaModule('cordova-lib').configparser,
        config = new ConfigParser(path.join(projectRoot, 'config.xml'));
        
    console.log('removing android foreground notification...');
    var packageNames = config.android_packageName() || config.packageName();
    var targetFile = path.join(projectRoot, 'platforms', 'android', 'src', packageNames.replace(/\./g, path.sep), 'MainActivity.java');

    var content = fs.readFileSync(targetFile, {encoding: 'utf8'});
    if(content.indexOf('com.mobishift.plugins.foregroundnotification.ForegroundNotification') !== -1){
        content = content.replace('import com.mobishift.plugins.foregroundnotification.ForegroundNotification;', '')
                        .replace('if(getIntent().hasExtra("foregroundUrl")){ForegroundNotification.setUrlPath(getIntent().getStringExtra("foregroundUrl"));}', '')
                        .replace('if(intent.hasExtra("foregroundUrl")){ForegroundNotification.setUrlPath(intent.getStringExtra("foregroundUrl"));}', '');
        
        fs.writeFileSync(targetFile, content);
    }
};