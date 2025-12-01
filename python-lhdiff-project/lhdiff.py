import difflib
import math
import re
import sys
import os

class LHDiff:
    def __init__(self, old_file_path, new_file_path):
        self.old_lines = self._read_file(old_file_path)
        self.new_lines = self._read_file(new_file_path)
        
        # Mappings: old_line_index (0-based) -> new_line_index (0-based)
        self.mapping = {} 
        
        self.matched_old = set()
        self.matched_new = set()

    def _determine_type(self, text):
        """Classifies a line as 'COMMENT' or 'CODE'."""
        stripped = text.strip()
        if stripped.startswith('#'):
            return 'COMMENT'
        return 'CODE'

    def _read_file(self, path):
        """Reads file and returns a list of dictionaries containing line info."""
        lines = []
        try:
            with open(path, 'r', encoding='utf-8') as f:
                content = f.readlines()
                for idx, line in enumerate(content):
                    is_empty = not line.strip()
                    norm, tokens = self._smart_normalize(line)
                    line_type = self._determine_type(line)
                    
                    lines.append({
                        'index': idx,
                        'content': line,
                        'normalized': norm,
                        'tokens': tokens,
                        'type': line_type,
                        'is_empty': is_empty
                    })
        except FileNotFoundError:
            print(f"Error: File {path} not found.")
            sys.exit(1)
        return lines

    def _smart_normalize(self, text):
        """
        Step 1: Preprocessing.
        """
        stripped = text.strip()
        if not stripped:
            return "", []

        if stripped.startswith('#'):
            norm_text = stripped.lower()
        else:
            code_part = text.split('#')[0]
            norm_text = code_part.strip().lower()

        # Standardize whitespace
        norm_text = re.sub(r'\s+', ' ', norm_text)
        
        # Tokenize for Context Vector
        tokens = re.findall(r'\w+', norm_text)
        
        return norm_text, tokens

    def _levenshtein_similarity(self, s1, s2):
        if not s1 and not s2:
            return 1.0
        return difflib.SequenceMatcher(None, s1, s2).ratio()

    def _cosine_similarity(self, vec1, vec2):
        intersection = set(vec1.keys()) & set(vec2.keys())
        numerator = sum([vec1[x] * vec2[x] for x in intersection])

        sum1 = sum([val**2 for val in vec1.values()])
        sum2 = sum([val**2 for val in vec2.values()])
        denominator = math.sqrt(sum1) * math.sqrt(sum2)

        if not denominator:
            return 0.0
        return float(numerator) / denominator

    def _get_context_vector(self, lines_list, target_idx, window=4):
        """Extracts context (4 lines up, 4 lines down)."""
        start = max(0, target_idx - window)
        end = min(len(lines_list), target_idx + window + 1)
        
        vector = {}
        for i in range(start, end):
            if i == target_idx: continue
            if lines_list[i]['is_empty']: continue
            
            for token in lines_list[i]['tokens']:
                vector[token] = vector.get(token, 0) + 1
        return vector

    def _calculate_combined_score(self, old_idx, new_idx):
        """
        Step 3: Calculate Score.
        Adjusted weights: 0.7 Content + 0.3 Context
        This prevents lines with identical context but different content (like comments)
        from falsely matching.
        """
        old_line = self.old_lines[old_idx]
        new_line = self.new_lines[new_idx]

        if old_line['type'] != new_line['type']:
            return 0.0

        content_sim = self._levenshtein_similarity(old_line['normalized'], new_line['normalized'])

        old_ctx_vec = self._get_context_vector(self.old_lines, old_idx)
        new_ctx_vec = self._get_context_vector(self.new_lines, new_idx)
        context_sim = self._cosine_similarity(old_ctx_vec, new_ctx_vec)

        # UPDATED WEIGHTS
        return 0.7 * content_sim + 0.3 * context_sim

    def run(self):
        """Main execution flow."""
        
        # --- Step 2: Detect Unchanged Lines (Exact Matches) ---
        old_indices = [i for i, l in enumerate(self.old_lines) if not l['is_empty']]
        new_indices = [i for i, l in enumerate(self.new_lines) if not l['is_empty']]
        
        old_hashes = [self.old_lines[i]['normalized'] for i in old_indices]
        new_hashes = [self.new_lines[i]['normalized'] for i in new_indices]

        matcher = difflib.SequenceMatcher(None, old_hashes, new_hashes)
        
        for tag, i1, i2, j1, j2 in matcher.get_opcodes():
            if tag == 'equal':
                for k in range(i2 - i1):
                    real_old = old_indices[i1 + k]
                    real_new = new_indices[j1 + k]
                    self.mapping[real_old] = real_new
                    self.matched_old.add(real_old)
                    self.matched_new.add(real_new)

        # --- Step 3 & 4: Generate Candidates & Resolve Conflicts ---
        
        # UPDATED THRESHOLD: 0.65
        # This ensures that completely different comments don't match even if context is perfect.
        THRESHOLD = 0.65
        
        unmatched_old = [i for i in range(len(self.old_lines)) 
                         if i not in self.matched_old and not self.old_lines[i]['is_empty']]
        unmatched_new = [j for j in range(len(self.new_lines)) 
                         if j not in self.matched_new and not self.new_lines[j]['is_empty']]
        
        candidates = []

        for o_idx in unmatched_old:
            for n_idx in unmatched_new:
                score = self._calculate_combined_score(o_idx, n_idx)
                if score >= THRESHOLD:
                    candidates.append({
                        'score': score,
                        'old': o_idx,
                        'new': n_idx
                    })
        
        candidates.sort(key=lambda x: x['score'], reverse=True)

        for cand in candidates:
            o_idx = cand['old']
            n_idx = cand['new']
            
            if o_idx not in self.matched_old and n_idx not in self.matched_new:
                self.mapping[o_idx] = n_idx
                self.matched_old.add(o_idx)
                self.matched_new.add(n_idx)

        # --- Step 5: Detect Line Splits ---
        unmatched_old = [i for i in range(len(self.old_lines)) 
                         if i not in self.matched_old and not self.old_lines[i]['is_empty']]
        
        sorted_unmatched_new = sorted([j for j in range(len(self.new_lines)) 
                                       if j not in self.matched_new and not self.new_lines[j]['is_empty']])
        
        for old_idx in unmatched_old:
            best_split_score = -1
            best_split_indices = []
            
            for k in range(len(sorted_unmatched_new)):
                def get_norm(idx): return self.new_lines[idx]['normalized']

                # 2-line split
                if k + 1 < len(sorted_unmatched_new):
                    idx1 = sorted_unmatched_new[k]
                    idx2 = sorted_unmatched_new[k+1]
                    
                    if idx2 - idx1 <= 3: 
                        merged_content = get_norm(idx1) + get_norm(idx2)
                        old_content = self.old_lines[old_idx]['normalized']
                        
                        sim = self._levenshtein_similarity(old_content, merged_content)
                        if sim > best_split_score and sim > 0.65:
                            best_split_score = sim
                            best_split_indices = [idx1, idx2]

                # 3-line split
                if k + 2 < len(sorted_unmatched_new):
                    idx1 = sorted_unmatched_new[k]
                    idx2 = sorted_unmatched_new[k+1]
                    idx3 = sorted_unmatched_new[k+2]
                    
                    if idx3 - idx1 <= 5: 
                        merged_content = get_norm(idx1) + get_norm(idx2) + get_norm(idx3)
                        old_content = self.old_lines[old_idx]['normalized']
                        
                        sim = self._levenshtein_similarity(old_content, merged_content)
                        if sim > best_split_score and sim > 0.65:
                            best_split_score = sim
                            best_split_indices = [idx1, idx2, idx3]

            if best_split_indices:
                self.mapping[old_idx] = best_split_indices
                self.matched_old.add(old_idx)
                for idx in best_split_indices:
                    self.matched_new.add(idx)

    def print_mapping(self):
        print(f"{'Old Line #':<12} {'New Line #'}")
        print("-" * 25)
        
        for i, line in enumerate(self.old_lines):
            if line['is_empty']: continue
                
            old_num = i + 1
            if i in self.mapping:
                target = self.mapping[i]
                if isinstance(target, list):
                    new_nums = ",".join([str(x+1) for x in target])
                    print(f"{old_num:<12} {new_nums}")
                else:
                    print(f"{old_num:<12} {target + 1}")
            else:
                print(f"{old_num:<12} -1")
        
        for j, line in enumerate(self.new_lines):
            if line['is_empty']: continue
            
            if j not in self.matched_new:
                new_num = j + 1
                print(f"{'-1':<12} {new_num}")

if __name__ == "__main__":
    if len(sys.argv) == 3:
        f1_path = sys.argv[1]
        f2_path = sys.argv[2]
        tracker = LHDiff(f1_path, f2_path)
        tracker.run()
        tracker.print_mapping()