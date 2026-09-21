const fs = require('fs');
const data = JSON.parse(fs.readFileSync('figma_clean.json', 'utf16le').replace(/^\uFEFF/, ''));

function findNodes(n, match) {
    let r = [];
    if (n.name && n.name.toLowerCase().includes(match)) {
        r.push(n);
    }
    if (n.children) {
        n.children.forEach(c => r.push(...findNodes(c, match)));
    }
    return r;
}

const targets = [];
['concept 03', 'concept 04', 'concept (03)', 'concept (04)', 'gemini use this', 'logo'].forEach(m => {
    targets.push(...findNodes(data.document, m));
});

function dump(n, depth = 0) {
    let ind = '  '.repeat(depth);
    let out = ind + n.name + ' (' + n.type + ')';
    if (n.characters) out += ' text:"' + n.characters.replace(/\n/g, '\\n') + '"';
    if (n.fills && n.fills.length) {
        let colors = n.fills.map(f => {
            if (f.type === 'SOLID' && f.color) {
                let r = Math.round(f.color.r * 255);
                let g = Math.round(f.color.g * 255);
                let b = Math.round(f.color.b * 255);
                let toHex = (c) => c.toString(16).padStart(2, '0').toUpperCase();
                return `#${toHex(r)}${toHex(g)}${toHex(b)}`;
            } else if (f.type && f.type.includes('GRADIENT')) {
                return 'GRADIENT';
            }
            return '';
        }).join(', ');
        if (colors) out += ' fills:' + colors;
    }
    if (n.style) {
        if (n.style.fontFamily) out += ' font:' + n.style.fontFamily;
        if (n.style.fontWeight) out += ' weight:' + n.style.fontWeight;
    }
    console.log(out);
    if (n.children) {
        n.children.forEach(c => dump(c, depth + 1));
    }
}

const uniqueTargets = [];
const seen = new Set();
for (let t of targets) {
    if (!seen.has(t.id)) {
        seen.add(t.id);
        uniqueTargets.push(t);
    }
}

uniqueTargets.forEach(t => dump(t));
