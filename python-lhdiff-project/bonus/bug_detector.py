import subprocess
import re
import os
import sys
import html

class SZZVisualizer:
    def __init__(self, repo_path):
        self.repo_path = repo_path
        self.fix_keywords = r"(?i)(fix|solved|closed|resolved|patch|bug|issue|error|typo|#\d+)"
        self.results = []
        self.repo_url = self.get_remote_url()

    def run_git(self, args):
        result = subprocess.run(
            ['git'] + args, 
            cwd=self.repo_path, 
            stdout=subprocess.PIPE, 
            stderr=subprocess.PIPE,
            text=True,
            errors='ignore'
        )
        return result.stdout.strip()

    def get_remote_url(self):
        """Auto-detects the GitHub/GitLab URL to create clickable links"""
        url = self.run_git(['config', '--get', 'remote.origin.url'])
        if url.endswith('.git'):
            url = url[:-4]
        return url

    def get_commit_log(self):
        raw_log = self.run_git(['log', '--pretty=format:%H|%s|%ad', '--date=short'])
        commits = []
        for line in raw_log.split('\n'):
            if line:
                parts = line.split('|', 2)
                if len(parts) == 3:
                    commits.append({'hash': parts[0], 'msg': parts[1], 'date': parts[2]})
        return commits

    def is_fix_commit(self, message):
        return re.search(self.fix_keywords, message) is not None

    def get_modified_lines_in_parent(self, commit_hash):
        diff_output = self.run_git(['show', '-U0', commit_hash])
        changes = []
        current_file = None
        chunk_header = re.compile(r"^@@ -(\d+)(?:,(\d+))? \+(\d+)(?:,(\d+))? @@")

        for line in diff_output.split('\n'):
            if line.startswith('--- a/'):
                current_file = line[6:]
            elif line.startswith('@@ '):
                match = chunk_header.match(line)
                if match and current_file:
                    start_line = int(match.group(1))
                    count = int(match.group(2)) if match.group(2) else 1
                    if count > 0:
                        changes.append({'file': current_file, 'start': start_line, 'end': start_line + count - 1})
        return changes

    def blame_line(self, file_path, line_number, parent_commit):
        output = self.run_git(['blame', '-L', f'{line_number},{line_number}', '--porcelain', parent_commit, '--', file_path])
        if output:
            return output.split('\n')[0].split(' ')[0]
        return None

    def analyze(self):
        print(f"Analyzing {self.repo_path}...")
        commits = self.get_commit_log()
        
        for commit in commits:
            if self.is_fix_commit(commit['msg']):
                parent_hash = self.run_git(['rev-parse', f'{commit["hash"]}^'])
                changed_blocks = self.get_modified_lines_in_parent(commit['hash'])
                
                suspects = set()
                evidence = []

                for change in changed_blocks:
                    f = change['file']
                    for line_num in range(change['start'], change['end'] + 1):
                        culprit = self.blame_line(f, line_num, parent_hash)
                        if culprit:
                            suspects.add(culprit)
                            evidence.append(f"Line {line_num} in {f}")

                if suspects:
                    self.results.append({
                        'fix_commit': commit,
                        'suspects': list(suspects),
                        'evidence': evidence
                    })
                    print(f"Found match: Fix {commit['hash'][:7]} -> {len(suspects)} suspects")

        self.generate_html_report()

    def generate_html_report(self):
        """Generates a nice HTML file to verify results automatically"""
        html_content = f"""
        <html>
        <head>
            <title>Bug Detector Report</title>
            <style>
                body {{ font-family: sans-serif; padding: 20px; background: #f4f4f9; }}
                h1 {{ color: #333; }}
                table {{ width: 100%; border-collapse: collapse; background: white; box-shadow: 0 1px 3px rgba(0,0,0,0.2); }}
                th, td {{ padding: 12px; border: 1px solid #ddd; text-align: left; }}
                th {{ background-color: #007bff; color: white; }}
                tr:nth-child(even) {{ background-color: #f9f9f9; }}
                .tag {{ background: #28a745; color: white; padding: 2px 6px; border-radius: 4px; font-size: 0.8em; }}
                .bad {{ background: #dc3545; color: white; padding: 2px 6px; border-radius: 4px; font-size: 0.8em; }}
                a {{ text-decoration: none; color: #007bff; font-weight: bold; }}
                a:hover {{ text-decoration: underline; }}
            </style>
        </head>
        <body>
            <h1>Bug Introducing Change Report</h1>
            <p>Analyzed Repo: <a href="{self.repo_url}" target="_blank">{self.repo_url}</a></p>
            <table>
                <thead>
                    <tr>
                        <th>Fix Commit (The Solution)</th>
                        <th>Evidence (Lines Traced)</th>
                        <th>Bug Introducing Commit (The Cause)</th>
                    </tr>
                </thead>
                <tbody>
        """

        for item in self.results:
            fix = item['fix_commit']
            fix_url = f"{self.repo_url}/commit/{fix['hash']}" if self.repo_url else "#"
            
            evidence_html = "<br>".join(item['evidence'][:5])
            if len(item['evidence']) > 5: evidence_html += "<br>...and more"

            suspect_links = []
            for s_hash in item['suspects']:
                s_details = self.run_git(['show', '-s', '--format=%s (%ad)', s_hash])
                s_url = f"{self.repo_url}/commit/{s_hash}" if self.repo_url else "#"
                suspect_links.append(f"""
                    <div style="margin-bottom: 8px;">
                        <span class="bad">BUG START</span> 
                        <a href="{s_url}" target="_blank">{s_hash[:7]}</a><br>
                        <small>{html.escape(s_details)}</small>
                    </div>
                """)

            html_content += f"""
                <tr>
                    <td valign="top">
                        <span class="tag">FIX</span> <a href="{fix_url}" target="_blank">{fix['hash'][:7]}</a><br>
                        <strong>{html.escape(fix['msg'])}</strong><br>
                        <small>{fix['date']}</small>
                    </td>
                    <td valign="top" style="font-family: monospace; font-size: 0.9em; color: #555;">
                        {evidence_html}
                    </td>
                    <td valign="top">
                        {"".join(suspect_links)}
                    </td>
                </tr>
            """

        html_content += """
                </tbody>
            </table>
        </body>
        </html>
        """

        output_file = os.path.join(self.repo_path, "szz_report.html")
        # If repo path is just "." write to current dir
        if self.repo_path == ".": output_file = "szz_report.html"
        
        with open("szz_report.html", "w", encoding="utf-8") as f:
            f.write(html_content)
        
        print(f"\n[SUCCESS] Report generated: {os.path.abspath('szz_report.html')}")
        print("Open this file in your browser to verify the results.")

if __name__ == "__main__":
    path = sys.argv[1] if len(sys.argv) > 1 else "."
    visualizer = SZZVisualizer(path)
    visualizer.analyze()