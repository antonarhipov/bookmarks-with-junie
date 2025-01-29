const fs = require('fs');
const path = require('path');

function addJsExtensions(directory) {
    const files = fs.readdirSync(directory);
    
    files.forEach(file => {
        const fullPath = path.join(directory, file);
        if (fs.statSync(fullPath).isDirectory()) {
            addJsExtensions(fullPath);
            return;
        }
        
        if (!file.endsWith('.js')) return;
        
        let content = fs.readFileSync(fullPath, 'utf8');
        content = content.replace(
            /from ['"](.+?)['"];/g,
            (match, importPath) => {
                if (importPath.startsWith('.') && !importPath.endsWith('.js')) {
                    return `from '${importPath}.js';`;
                }
                return match;
            }
        );
        
        fs.writeFileSync(fullPath, content);
    });
}

// Start from the dist directory
addJsExtensions(path.join(__dirname, '../js/dist'));